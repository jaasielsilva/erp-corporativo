package com.jaasielsilva.portalceo.util;

public final class CnpjUtils {
    private CnpjUtils() {}

    public static String sanitize(String cnpj) {
        if (cnpj == null) return null;
        return cnpj.replaceAll("[^0-9]", "");
    }

    public static boolean isValidLength(String cnpj) {
        String s = sanitize(cnpj);
        return s != null && s.length() == 14 && !s.matches("0{14}");
    }

    public static boolean isValid(String cnpj) {
        String s = sanitize(cnpj);
        if (s == null || s.length() != 14 || s.matches("0{14}")) return false;
        int[] pesos1 = {5,4,3,2,9,8,7,6,5,4,3,2};
        int[] pesos2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
        int soma1 = 0;
        for (int i = 0; i < 12; i++) soma1 += (s.charAt(i) - '0') * pesos1[i];
        int dig1 = soma1 % 11;
        dig1 = dig1 < 2 ? 0 : 11 - dig1;
        int soma2 = 0;
        for (int i = 0; i < 13; i++) soma2 += (s.charAt(i) - '0') * pesos2[i];
        int dig2 = soma2 % 11;
        dig2 = dig2 < 2 ? 0 : 11 - dig2;
        return (s.charAt(12) - '0') == dig1 && (s.charAt(13) - '0') == dig2;
    }
}
