package org.aut.helpers;

import net.datafaker.Faker;

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
}
