package com.kab.qershi.common.util;

/**
 * Utility class providing PII (Personally Identifiable Information) masking rules
 * for secure logging and data protection across Qershi Link microservices.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public final class PiiMasker {

    private PiiMasker() {
        // Utility class
    }

    /**
     * Masks phone number, preserving country prefix and last 4 digits.
     * Example: "+251911223344" -> "+2519****3344"
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return "*****";
        int len = phone.length();
        if (len <= 6) return "*****";
        return phone.substring(0, 5) + "****" + phone.substring(len - 4);
    }

    /**
     * Masks email address, preserving first letter, last letter of local part, and domain.
     * Example: "john.doe@gmail.com" -> "j***e@gmail.com"
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "*****@*****";
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }

    /**
     * Masks government ID numbers, preserving first 2 and last 4 characters.
     * Example: "ID-98765432" -> "ID-****5432"
     */
    public static String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.isBlank()) return "****";
        int len = idNumber.length();
        if (len <= 4) return "****";
        return idNumber.substring(0, 2) + "****" + idNumber.substring(len - 4);
    }

    /**
     * Masks Tax Identification Numbers (TIN), preserving first 3 and last 3 digits.
     * Example: "1234567890" -> "123****890"
     */
    public static String maskTin(String tin) {
        if (tin == null || tin.isBlank()) return "****";
        int len = tin.length();
        if (len <= 6) return "****";
        return tin.substring(0, 3) + "****" + tin.substring(len - 3);
    }

    /**
     * Masks names, preserving initial letter and trailing letter.
     * Example: "Johnathan" -> "J***n"
     */
    public static String maskName(String name) {
        if (name == null || name.isBlank()) return "***";
        int len = name.trim().length();
        if (len <= 2) return name.substring(0, 1) + "*";
        return name.charAt(0) + "***" + name.charAt(len - 1);
    }
}
