package com.lms.util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;

/** Wraps BCrypt so no other layer touches hashing details directly. */
public class PasswordUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String RANDOM_PASSWORD_CHARS =
            "abcdefghijkmnpqrstuvwxyz" + // no l/o, avoids visual ambiguity
                    "ABCDEFGHJKLMNPQRSTUVWXYZ" +
                    "23456789" +
                    "!@#$%";

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /** Generates a random, human-typeable temporary password (default length 10). */
    public static String generateRandomPassword() {
        return generateRandomPassword(10);
    }

    public static String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(RANDOM_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
