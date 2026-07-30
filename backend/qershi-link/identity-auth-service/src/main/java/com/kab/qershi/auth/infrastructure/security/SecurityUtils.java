package com.kab.qershi.auth.infrastructure.security;

import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

/**
 * Helper utility to extract authenticated user roles and tenant claims from SecurityContext.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class SecurityUtils {

    /**
     * Checks if the authenticated principal possesses global SUPER_ADMIN role authority.
     */
    public static boolean isSuperAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    /**
     * Extracts tenant saccoId from authentication details map stored during JWT filter processing.
     */
    public static UUID extractSaccoId(Authentication auth) {
        if (auth == null || auth.getDetails() == null) return null;
        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object saccoIdObj = details.get("saccoId");
            if (saccoIdObj != null) {
                try {
                    return UUID.fromString(saccoIdObj.toString().trim());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    /**
     * Extracts authenticated user ID from authentication details map.
     */
    public static UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getDetails() == null) return null;
        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object userIdObj = details.get("userId");
            if (userIdObj != null) {
                try {
                    return UUID.fromString(userIdObj.toString().trim());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }
}
