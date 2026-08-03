package com.kab.qershi.account.domain.service;

import org.springframework.stereotype.Service;

/**
 * Domain Service implementing Enterprise Core Banking Account Number Generation & Verification.
 * Format: [4-DIGIT SACCO_CODE]-[3-DIGIT BRANCH_CODE]-[3-DIGIT PRODUCT_CODE]-[7-DIGIT SEQUENCE + LUHN CHECK DIGIT]
 * Example: 0001-002-101-0001429
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class AccountNumberGenerator {

    /**
     * Generates a structured enterprise core banking account number.
     *
     * @param saccoCode 4-digit regulatory SACCO code (e.g. "0001")
     * @param branchCode 3-digit branch/district code (e.g. "002")
     * @param productCode 3-digit product code (e.g. "101")
     * @param sequenceNumber Auto-incrementing account sequence number (e.g. 142)
     * @return String Formatted account number (e.g. "0001-002-101-0001429")
     */
    public String generateAccountNo(String saccoCode, String branchCode, String productCode, long sequenceNumber) {
        String cleanSacco = padAndSanitize(saccoCode, 4);
        String cleanBranch = padAndSanitize(branchCode, 3);
        String cleanProduct = padAndSanitize(productCode, 3);

        // Format 6-digit base sequence (e.g., 142 -> "000142")
        String baseSequence = String.format("%06d", sequenceNumber);

        // Compute Modulo-10 (Luhn) check digit over the raw numeric string
        String rawNumeric = cleanSacco.replaceAll("\\D", "") +
                cleanBranch.replaceAll("\\D", "") +
                cleanProduct.replaceAll("\\D", "") +
                baseSequence;

        int checkDigit = calculateLuhnCheckDigit(rawNumeric);

        return String.format("%s-%s-%s-%s%d", cleanSacco, cleanBranch, cleanProduct, baseSequence, checkDigit);
    }

    /**
     * Validates an account number's Luhn check digit.
     */
    public boolean validateAccountNo(String accountNo) {
        if (accountNo == null || accountNo.isBlank()) {
            return false;
        }
        String cleanNumeric = accountNo.replaceAll("\\D", "");
        if (cleanNumeric.length() < 7) {
            return false;
        }
        return validateLuhn(cleanNumeric);
    }

    private String padAndSanitize(String input, int length) {
        if (input == null || input.isBlank()) {
            return "0".repeat(length);
        }
        String clean = input.trim();
        if (clean.length() < length) {
            return "0".repeat(length - clean.length()) + clean;
        }
        return clean.substring(0, length);
    }

    /**
     * Calculates Luhn Modulo-10 check digit.
     */
    private int calculateLuhnCheckDigit(String numberString) {
        int sum = 0;
        boolean alternate = true;

        for (int i = numberString.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(numberString.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    private boolean validateLuhn(String numberString) {
        int sum = 0;
        boolean alternate = false;

        for (int i = numberString.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(numberString.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}
