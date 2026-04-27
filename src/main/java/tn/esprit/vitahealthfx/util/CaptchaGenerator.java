package tn.esprit.vitahealthfx.util;

import java.util.Random;

public class CaptchaGenerator {
    public static String generateCaptcha() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}