package tn.esprit.workshopjdbc.Utils;

import java.util.Random;

public class CaptchaGenerator {
    
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final Random random = new Random();
    
    public static String generateCaptcha(int length) {
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARS.length());
            captcha.append(CHARS.charAt(index));
        }
        return captcha.toString();
    }
    
    public static boolean validateCaptcha(String input, String generated) {
        return input != null && generated != null && input.equals(generated);
    }
}