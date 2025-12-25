package com.jaasielsilva.portalceo.service.whatchat;

public final class PhoneNormalizer {
    private PhoneNormalizer() {}

    public static String digitsOnly(String input) {
        if (input == null) {
            return null;
        }
        String digits = input.replaceAll("\\D+", "");
        return digits.isBlank() ? null : digits;
    }
}

