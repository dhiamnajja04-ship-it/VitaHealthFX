package tn.esprit.workshopjdbc.Utils;

import java.util.Random;

public class CaptchaGenerator {
    public static String generateCaptcha() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}