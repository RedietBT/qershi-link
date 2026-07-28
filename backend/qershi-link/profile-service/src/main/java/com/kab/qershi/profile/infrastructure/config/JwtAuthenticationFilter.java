package com.kab.qershi.profile.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;

/**
 * Servlet Filter intercepting HTTP requests to validate JWT Bearer tokens,
 * populate Spring SecurityContext authorities, and populate TenantContext.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    // 1. Role claim
                    String role = claims.get("role", String.class);
                    if (role != null && !role.isBlank()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }

                    // 2. Permissions / Authorities array claim
                    List<?> permissions = claims.get("permissions", List.class);
                    if (permissions != null) {
                        for (Object perm : permissions) {
                            if (perm != null) {
                                authorities.add(new SimpleGrantedAuthority(perm.toString()));
                            }
                        }
                    }

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
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
            return null;
        }
    }
}
