package tn.esprit.workshopjdbc.Services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocialAuthService {
    private static final String DEFAULT_REDIRECT_URI = "http://localhost:8085/oauth/callback";
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(20);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();

    public enum Provider {
        GOOGLE("Google", "GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET",
                "https://accounts.google.com/o/oauth2/v2/auth",
                "https://oauth2.googleapis.com/token",
                "https://www.googleapis.com/oauth2/v3/userinfo",
                "openid email profile"),
        FACEBOOK("Facebook", "FACEBOOK_APP_ID", "FACEBOOK_APP_SECRET",
                "https://www.facebook.com/v20.0/dialog/oauth",
                "https://graph.facebook.com/v20.0/oauth/access_token",
                "https://graph.facebook.com/me?fields=id,name,email",
                "email,public_profile");

        private final String label;
        private final String clientIdEnv;
        private final String clientSecretEnv;
        private final String authUrl;
        private final String tokenUrl;
        private final String profileUrl;
        private final String scope;

        Provider(String label, String clientIdEnv, String clientSecretEnv,
                 String authUrl, String tokenUrl, String profileUrl, String scope) {
            this.label = label;
            this.clientIdEnv = clientIdEnv;
            this.clientSecretEnv = clientSecretEnv;
            this.authUrl = authUrl;
            this.tokenUrl = tokenUrl;
            this.profileUrl = profileUrl;
            this.scope = scope;
        }

        public String label() {
            return label;
        }
    }

    public AuthStartResult start(Provider provider, String context) {
        String missing = missingConfig(provider);
        if (!missing.isBlank()) {
            return AuthStartResult.error(missing);
        }
        if (!canOpenBrowser()) {
            return AuthStartResult.error("Ouverture navigateur indisponible sur ce poste.");
        }
        try {
            Desktop.getDesktop().browse(URI.create(buildAuthorizationUrl(provider, context, randomState(), null)));
            return AuthStartResult.started(provider.label + " ouvert dans le navigateur.");
        } catch (IOException | IllegalArgumentException e) {
            return AuthStartResult.error("Impossible d'ouvrir " + provider.label + ": " + e.getMessage());
        }
    }

    public CompletableFuture<AuthResult> authenticateAsync(Provider provider, String context) {
        return CompletableFuture.supplyAsync(() -> authenticate(provider, context));
    }

    private AuthResult authenticate(Provider provider, String context) {
        String missing = missingConfig(provider);
        if (!missing.isBlank()) {
            return AuthResult.error(provider, missing);
        }
        if (!canOpenBrowser()) {
            return AuthResult.error(provider, "Ouverture navigateur indisponible sur ce poste.");
        }

        URI redirectUri = URI.create(redirectUri());
        String path = redirectUri.getPath() == null || redirectUri.getPath().isBlank()
                ? "/oauth/callback"
                : redirectUri.getPath();
        String state = randomState();
        String codeVerifier = provider == Provider.GOOGLE ? codeVerifier() : null;
        String codeChallenge = provider == Provider.GOOGLE ? codeChallenge(codeVerifier) : null;

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", redirectUri.getPort()), 0);
            CompletableFuture<Map<String, String>> callbackFuture = new CompletableFuture<>();
            server.createContext(path, exchange -> handleCallback(exchange, callbackFuture));
            server.setExecutor(Executors.newSingleThreadExecutor());
            server.start();
            try {
                Desktop.getDesktop().browse(URI.create(buildAuthorizationUrl(provider, context, state, codeChallenge)));
                Map<String, String> params = callbackFuture.orTimeout(120, java.util.concurrent.TimeUnit.SECONDS).join();
                if (params.containsKey("error")) {
                    return AuthResult.error(provider, provider.label + " a refuse la connexion: " + params.get("error"));
                }
                if (!state.equals(params.get("state"))) {
                    return AuthResult.error(provider, "Verification OAuth invalide: state incorrect.");
                }
                String code = params.get("code");
                if (code == null || code.isBlank()) {
                    return AuthResult.error(provider, "Aucun code OAuth recu depuis " + provider.label + ".");
                }

                String token = exchangeCodeForToken(provider, code, codeVerifier);
                OAuthUser profile = fetchProfile(provider, token);
                return AuthResult.success(provider, profile);
            } finally {
                server.stop(0);
            }
        } catch (Exception e) {
            return AuthResult.error(provider, "Connexion " + provider.label + " impossible: " + rootMessage(e));
        }
    }

    private void handleCallback(HttpExchange exchange, CompletableFuture<Map<String, String>> future) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
        future.complete(params);
        String html = """
                <html><body style="font-family:Arial;padding:32px">
                <h2>VitaHealthFX</h2>
                <p>Connexion recue. Vous pouvez revenir a l'application.</p>
                </body></html>
                """;
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, html.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(html.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String buildAuthorizationUrl(Provider provider, String context, String state, String codeChallenge) {
        String clientId = env(provider.clientIdEnv, "");
        String url = provider.authUrl
                + "?client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri())
                + "&response_type=code"
                + "&scope=" + enc(provider.scope)
                + "&state=" + enc(state);
        if (provider == Provider.GOOGLE) {
            url += "&access_type=offline&prompt=select_account";
            if (codeChallenge != null && !codeChallenge.isBlank()) {
                url += "&code_challenge=" + enc(codeChallenge) + "&code_challenge_method=S256";
            }
        }
        return url;
    }

    private String exchangeCodeForToken(Provider provider, String code, String codeVerifier) throws IOException, InterruptedException {
        String clientId = env(provider.clientIdEnv, "");
        String clientSecret = env(provider.clientSecretEnv, "");
        HttpRequest request;
        if (provider == Provider.GOOGLE) {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("code", code);
            values.put("client_id", clientId);
            if (!clientSecret.isBlank()) {
                values.put("client_secret", clientSecret);
            }
            values.put("redirect_uri", redirectUri());
            values.put("grant_type", "authorization_code");
            if (codeVerifier != null && !codeVerifier.isBlank()) {
                values.put("code_verifier", codeVerifier);
            }
            String form = form(values);
            request = HttpRequest.newBuilder(URI.create(provider.tokenUrl))
                    .timeout(HTTP_TIMEOUT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
        } else {
            String url = provider.tokenUrl
                    + "?client_id=" + enc(clientId)
                    + "&redirect_uri=" + enc(redirectUri())
                    + "&client_secret=" + enc(clientSecret)
                    + "&code=" + enc(code);
            request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();
        }

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("token refuse (" + response.statusCode() + "): " + response.body());
        }
        String token = jsonValue(response.body(), "access_token");
        if (token.isBlank()) {
            throw new IOException("access_token absent dans la reponse OAuth.");
        }
        return token;
    }

    private OAuthUser fetchProfile(Provider provider, String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(provider.profileUrl))
                .timeout(HTTP_TIMEOUT)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("profil refuse (" + response.statusCode() + "): " + response.body());
        }

        String body = response.body();
        String id = jsonValue(body, provider == Provider.GOOGLE ? "sub" : "id");
        String email = jsonValue(body, "email");
        String name = jsonValue(body, "name");
        String firstName = jsonValue(body, "given_name");
        String lastName = jsonValue(body, "family_name");
        if ((firstName.isBlank() || lastName.isBlank()) && !name.isBlank()) {
            String[] parts = name.trim().split("\\s+", 2);
            if (firstName.isBlank()) firstName = parts[0];
            if (lastName.isBlank() && parts.length > 1) lastName = parts[1];
        }
        if (email.isBlank()) {
            throw new IOException("email absent dans le profil " + provider.label + ".");
        }
        return new OAuthUser(provider.name(), id, email, firstName, lastName, name);
    }

    private String missingConfig(Provider provider) {
        if (env(provider.clientIdEnv, "").isBlank()) {
            return provider.label + " n'est pas configure. Ajoutez " + provider.clientIdEnv + ".";
        }
        if (provider != Provider.GOOGLE && env(provider.clientSecretEnv, "").isBlank()) {
            return provider.label + " n'est pas configure. Ajoutez " + provider.clientSecretEnv + ".";
        }
        URI uri = URI.create(redirectUri());
        if (uri.getPort() <= 0) {
            return "OAUTH_REDIRECT_URI doit contenir un port, ex: " + DEFAULT_REDIRECT_URI;
        }
        return "";
    }

    private static boolean canOpenBrowser() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    private static String redirectUri() {
        return env("OAUTH_REDIRECT_URI", DEFAULT_REDIRECT_URI);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> values = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return values;
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            String value = idx >= 0 ? pair.substring(idx + 1) : "";
            values.put(urlDecode(key), urlDecode(value));
        }
        return values;
    }

    private static String jsonValue(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(json == null ? "" : json);
        return matcher.find() ? unescapeJson(matcher.group(1)) : "";
    }

    private static String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\/", "/")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static String form(Map<String, String> values) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!sb.isEmpty()) sb.append('&');
            sb.append(enc(entry.getKey())).append('=').append(enc(entry.getValue()));
        }
        return sb.toString();
    }

    private static String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String urlDecode(String value) {
        return java.net.URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String safe(String context) {
        return context == null || context.isBlank() ? "auth" : context.replaceAll("[^a-zA-Z0-9_-]", "-");
    }

    private static String randomState() {
        return UUID.randomUUID() + "-" + RANDOM.nextInt(100000);
    }

    private static String codeVerifier() {
        byte[] bytes = new byte[64];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String codeChallenge(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key.toLowerCase(Locale.ROOT).replace('_', '.'));
        }
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    public record OAuthUser(String provider, String providerId, String email,
                            String firstName, String lastName, String fullName) {}

    public record AuthResult(boolean success, Provider provider, OAuthUser user, String message) {
        public static AuthResult success(Provider provider, OAuthUser user) {
            return new AuthResult(true, provider, user, "Connexion " + provider.label + " reussie.");
        }

        public static AuthResult error(Provider provider, String message) {
            return new AuthResult(false, provider, null, message);
        }
    }

    public record AuthStartResult(boolean started, String message) {
        public static AuthStartResult started(String message) {
            return new AuthStartResult(true, message);
        }

        public static AuthStartResult error(String message) {
            return new AuthStartResult(false, message);
        }
    }
}
