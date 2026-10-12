package org.aut.helpers;

import net.datafaker.Faker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class RegistrationHelper {

    private static final String EMAIL_DOMAIN = "@example.com";
    private static final String EMAIL_LOCAL_PART_REGEX = "[a-z0-9]{10}";
    private static final String PASSWORD_REGEX = "[a-zA-Z0-9]{6,11}";

    private final Faker faker = new Faker();

    /**
     * Builds fake registration data.
     *
     * @return a map with "email" (random value @example.com) and "password"
     *         (alphanumeric, 6 to 11 characters)
     */
    public Map<String, String> getRegisterMap() {

        Map<String, String> registerMap = new HashMap<String, String>();

        registerMap.put("email", faker.regexify(EMAIL_LOCAL_PART_REGEX) + EMAIL_DOMAIN);
        registerMap.put("password", faker.regexify(PASSWORD_REGEX));

        return registerMap;
    }

    /**
     * Generates the MD5 hash of a given text.
     *
     * @param text the text to hash
     * @return the MD5 hash as a 32-character lowercase hexadecimal string
     */
    public String generateMd5(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder md5 = new StringBuilder();
            for (byte b : hash) {
                md5.append(String.format("%02x", b));
            }

            return md5.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}
