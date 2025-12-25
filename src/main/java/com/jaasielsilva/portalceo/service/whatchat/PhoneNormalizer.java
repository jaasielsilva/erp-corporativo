package com.jaasielsilva.portalceo.service.whatchat;

public final class PhoneNormalizer {
    private PhoneNormalizer() {
    }

    public static String digitsOnly(String input) {
        if (input == null) {
            return null;
        }
        String digits = input.replaceAll("\\D+", "");
        return digits.isBlank() ? null : digits;
    }

    public static String whatsAppRemoteJidToDigits(String remoteJid) {
        if (remoteJid == null || remoteJid.isBlank()) {
            return null;
        }
        String base = remoteJid;
        int at = base.indexOf('@');
        if (at > 0) {
            base = base.substring(0, at);
        }
        return digitsOnly(base);
    }

    public static String toE164(String rawDigitsOrJid, String defaultCountryCode) {
        String digits = whatsAppRemoteJidToDigits(rawDigitsOrJid);
        if (digits == null) {
            return null;
        }
        String cc = digitsOnly(defaultCountryCode);
        if (cc == null) {
            cc = "55";
        }
        if (digits.startsWith(cc)) {
            return "+" + digits;
        }
        if (digits.length() == 10 || digits.length() == 11) {
            return "+" + cc + digits;
        }
        return "+" + digits;
    }

    public static boolean samePhone(String a, String b) {
        String da = digitsOnly(a);
        String db = digitsOnly(b);
        if (da == null || db == null) {
            return false;
        }
        if (da.equals(db)) {
            return true;
        }
        if (da.length() >= 11 && db.length() >= 11
                && da.substring(da.length() - 11).equals(db.substring(db.length() - 11))) {
            return true;
        }
        if (da.length() >= 10 && db.length() >= 10
                && da.substring(da.length() - 10).equals(db.substring(db.length() - 10))) {
            return true;
        }
        return false;
    }
}
