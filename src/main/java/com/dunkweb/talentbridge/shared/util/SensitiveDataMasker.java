package com.dunkweb.talentbridge.shared.util;

public final class SensitiveDataMasker {

    private SensitiveDataMasker() {
    }

    public static String maskEmail(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }

        String normalized = value.trim();
        int atIndex = normalized.indexOf('@');
        if (atIndex <= 1 || atIndex == normalized.length() - 1) {
            return maskSecret(normalized);
        }

        return normalized.charAt(0) + "***" + normalized.substring(atIndex);
    }

    public static String maskSecret(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }

        String normalized = value.trim();
        if (normalized.length() <= 2) {
            return "**";
        }

        return normalized.charAt(0) + "***" + normalized.charAt(normalized.length() - 1);
    }
}
