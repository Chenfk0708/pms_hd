package com.jeez.common.utils;

import java.util.Locale;
import java.util.regex.Pattern;

public final class InputValidationUtils {

    private static final Pattern PERSON_NAME_PATTERN = Pattern.compile("^[\\p{L}·\\s]{2,30}$");
    private static final Pattern MAINLAND_MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern PREFIXED_MAINLAND_MOBILE_PATTERN =
            Pattern.compile("^(?:\\+?86[-\\s]?|0086[-\\s]?)1[3-9]\\d{9}$");
    private static final Pattern LANDLINE_PATTERN = Pattern.compile("^0\\d{2,3}-?\\d{7,8}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern GENERAL_CREDENTIAL_PATTERN = Pattern.compile("^[A-Za-z0-9-]{5,20}$");

    private InputValidationUtils() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static boolean isValidPersonName(String value) {
        String normalized = trimToNull(value);
        return normalized != null
                && PERSON_NAME_PATTERN.matcher(normalized).matches()
                && normalized.codePoints().anyMatch(Character::isLetter);
    }

    public static boolean isValidMainlandMobile(String value) {
        String normalized = trimToNull(value);
        return normalized != null && MAINLAND_MOBILE_PATTERN.matcher(normalized).matches();
    }

    public static boolean isValidOptionalMainlandMobile(String value) {
        String normalized = trimToNull(value);
        return normalized == null || isValidMainlandMobile(normalized);
    }

    public static boolean isValidContactPhone(String value) {
        String normalized = trimToNull(value);
        return normalized != null
                && (MAINLAND_MOBILE_PATTERN.matcher(normalized).matches()
                || PREFIXED_MAINLAND_MOBILE_PATTERN.matcher(normalized).matches()
                || LANDLINE_PATTERN.matcher(normalized).matches());
    }

    public static boolean isValidOptionalContactPhone(String value) {
        String normalized = trimToNull(value);
        return normalized == null || isValidContactPhone(normalized);
    }

    public static boolean isValidOptionalEmail(String value) {
        String normalized = trimToNull(value);
        return normalized == null || EMAIL_PATTERN.matcher(normalized).matches();
    }

    public static boolean isValidCredential(String credentialType, String credentialNumber) {
        String normalizedNumber = trimToNull(credentialNumber);
        if (normalizedNumber == null) {
            return true;
        }
        String normalizedType = normalizeCredentialType(credentialType);
        if ("居民身份证".equals(normalizedType)) {
            return isValidResidentIdCard(normalizedNumber);
        }
        return GENERAL_CREDENTIAL_PATTERN.matcher(normalizedNumber).matches();
    }

    public static boolean isValidResidentIdCard(String value) {
        String normalized = trimToNull(value);
        if (normalized == null || !normalized.matches("^\\d{17}[\\dXx]$")) {
            return false;
        }
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int index = 0; index < weights.length; index++) {
            sum += Character.digit(normalized.charAt(index), 10) * weights[index];
        }
        char expected = checkCodes[sum % 11];
        char actual = Character.toUpperCase(normalized.charAt(17));
        return expected == actual;
    }

    public static String normalizeCredentialType(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return "居民身份证";
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if ("passport".equals(lower)) {
            return "Passport";
        }
        if ("居民身份证".equals(normalized)
                || "身份证".equals(normalized)
                || "resident_id_card".equals(lower)
                || "residentidcard".equals(lower)
                || "identity_card".equals(lower)
                || "id_card".equals(lower)) {
            return "居民身份证";
        }
        return normalized;
    }
}
