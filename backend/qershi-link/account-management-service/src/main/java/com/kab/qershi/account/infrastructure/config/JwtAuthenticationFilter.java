package com.kab.qershi.account.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Servlet Filter intercepting HTTP requests to validate JWT Bearer tokens,
 * populate Spring SecurityContext authorities (roles & permissions), extract userId principal, and populate TenantContext.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                Claims claims = parseClaims(token);
                if (claims != null) {
                    String username = claims.getSubject();
                    String userIdStr = claims.get("userId", String.class);

                    Object principal = username;
                    if (userIdStr != null && !userIdStr.isBlank()) {
                        try {
                            principal = UUID.fromString(userIdStr.trim());
                        } catch (Exception ignored) {}
                    }

                    Set<SimpleGrantedAuthority> authorities = new HashSet<>();

                    // 1. Check "authorities" claim (used by Identity Auth Service)
                    List<?> authoritiesClaim = claims.get("authorities", List.class);
                    if (authoritiesClaim != null) {
                        for (Object auth : authoritiesClaim) {
                            if (auth != null && !auth.toString().isBlank()) {
                                authorities.add(new SimpleGrantedAuthority(auth.toString().trim()));
                            }
                        }
                    }

                    // 2. Check "permissions" claim
                    List<?> permissionsClaim = claims.get("permissions", List.class);
                    if (permissionsClaim != null) {
                        for (Object perm : permissionsClaim) {
                            if (perm != null && !perm.toString().isBlank()) {
                                authorities.add(new SimpleGrantedAuthority(perm.toString().trim()));
                            }
                        }
                    }

                    // 3. Check "role" or "roles" claims
                    String role = claims.get("role", String.class);
                    if (role != null && !role.isBlank()) {
                        String roleAuth = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        authorities.add(new SimpleGrantedAuthority(roleAuth));
                    }

                    List<?> rolesClaim = claims.get("roles", List.class);
                    if (rolesClaim != null) {
                        for (Object r : rolesClaim) {
                            if (r != null && !r.toString().isBlank()) {
                                String roleAuth = r.toString().startsWith("ROLE_") ? r.toString() : "ROLE_" + r.toString();
                                authorities.add(new SimpleGrantedAuthority(roleAuth));
                            }
                        }
                    }

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, new ArrayList<>(authorities));
                    if (userIdStr != null && !userIdStr.isBlank()) {
                        auth.setDetails(userIdStr.trim());
                    }

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Populated SecurityContext for principal {} with authorities: {}", principal, authorities);
                }
            }

            // Also check X-Tenant-ID header to populate tenant context
            String tenantHeader = request.getHeader("X-Tenant-ID");
            if (tenantHeader != null && !tenantHeader.isBlank()) {
                TenantContext.setTenantSchema(tenantHeader.trim());
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Claims parseClaims(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            log.warn("Failed to parse JWT token in account-service: {}", ex.getMessage());
            return null;
        }
    }
}
