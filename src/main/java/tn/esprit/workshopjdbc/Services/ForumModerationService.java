package tn.esprit.workshopjdbc.Services;

import java.util.Arrays;
import java.util.List;

public class ForumModerationService {
    private static final List<String> BLOCKED_WORDS = Arrays.asList(
            "insulte", "violence", "haine", "spam", "arnaque"
    );

    private static final List<String> SENSITIVE_MEDICAL_WORDS = Arrays.asList(
            "suicide", "overdose", "urgence", "douleur thoracique", "saignement"
    );

    public String evaluateStatus(String title, String content) {
        String text = ((title == null ? "" : title) + " " + (content == null ? "" : content)).toLowerCase();

        for (String word : BLOCKED_WORDS) {
            if (text.contains(word)) {
                return "PENDING_REVIEW";
            }
        }

        for (String word : SENSITIVE_MEDICAL_WORDS) {
            if (text.contains(word)) {
                return "PENDING_REVIEW";
            }
        }

        if (text.length() > 2500) {
            return "PENDING_REVIEW";
        }

        return "PUBLISHED";
    }
}
