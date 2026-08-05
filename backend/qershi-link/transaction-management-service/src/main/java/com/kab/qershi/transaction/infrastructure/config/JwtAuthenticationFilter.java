package com.kab.qershi.transaction.infrastructure.config;

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

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet Filter validating JWT Bearer tokens and automatically binding active SACCO tenant schema to TenantContext.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final DataSource dataSource;
    private final ConcurrentHashMap<UUID, String> saccoSchemaCache = new ConcurrentHashMap<>();

    public JwtAuthenticationFilter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

                    List<?> authoritiesClaim = claims.get("authorities", List.class);
                    if (authoritiesClaim != null) {
                        for (Object auth : authoritiesClaim) {
                            if (auth != null && !auth.toString().isBlank()) {
                                String val = auth.toString().trim();
                                authorities.add(new SimpleGrantedAuthority(val));
                                if (val.startsWith("ROLE_")) {
                                    authorities.add(new SimpleGrantedAuthority(val.substring(5)));
                                } else {
                                    authorities.add(new SimpleGrantedAuthority("ROLE_" + val));
                                }
                            }
                        }
                    }

                    List<?> permissionsClaim = claims.get("permissions", List.class);
                    if (permissionsClaim != null) {
                        for (Object perm : permissionsClaim) {
                            if (perm != null && !perm.toString().isBlank()) {
                                String val = perm.toString().trim();
                                authorities.add(new SimpleGrantedAuthority(val));
                                if (val.startsWith("ROLE_")) {
                                    authorities.add(new SimpleGrantedAuthority(val.substring(5)));
                                } else {
                                    authorities.add(new SimpleGrantedAuthority("ROLE_" + val));
                                }
                            }
                        }
                    }

                    String role = claims.get("role", String.class);
                    if (role != null && !role.isBlank()) {
                        String r = role.trim();
                        authorities.add(new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r));
                        authorities.add(new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r.substring(5) : r));
                    }

                    List<?> rolesClaim = claims.get("roles", List.class);
                    if (rolesClaim != null) {
                        for (Object r : rolesClaim) {
                            if (r != null && !r.toString().isBlank()) {
                                String val = r.toString().trim();
                                authorities.add(new SimpleGrantedAuthority(val.startsWith("ROLE_") ? val : "ROLE_" + val));
                                authorities.add(new SimpleGrantedAuthority(val.startsWith("ROLE_") ? val.substring(5) : val));
                            }
                        }
                    }

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, new ArrayList<>(authorities));
                    if (userIdStr != null && !userIdStr.isBlank()) {
                        auth.setDetails(userIdStr.trim());
                    }

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    String saccoSchema = claims.get("saccoSchema", String.class);
                    if (saccoSchema != null && !saccoSchema.isBlank()) {
                        TenantContext.setTenantSchema(saccoSchema.trim());
                    } else {
                        String saccoIdStr = claims.get("saccoId", String.class);
                        if (saccoIdStr != null && !saccoIdStr.isBlank()) {
                            try {
                                UUID saccoId = UUID.fromString(saccoIdStr.trim());
                                String resolvedSchema = saccoSchemaCache.computeIfAbsent(saccoId, this::lookupSaccoSchema);
                                if (resolvedSchema != null && !resolvedSchema.isBlank()) {
                                    TenantContext.setTenantSchema(resolvedSchema);
                                }
                            } catch (Exception ex) {
                                log.warn("Failed parsing saccoId claim: {}", ex.getMessage());
                            }
                        }
                    }
                }
            }

            if (TenantContext.getTenantSchema() == null || TenantContext.getTenantSchema().equals(TenantContext.DEFAULT_TENANT)) {
                String tenantHeader = request.getHeader("X-Tenant-ID");
                if (tenantHeader != null && !tenantHeader.isBlank()) {
                    TenantContext.setTenantSchema(tenantHeader.trim());
                }
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
            log.warn("Failed to parse JWT token in transaction-service: {}", ex.getMessage());
            return null;
        }
    }

    private String lookupSaccoSchema(UUID saccoId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT schema_name FROM master_schema.sacco_registry WHERE sacco_id = ?")) {
            ps.setObject(1, saccoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String schema = rs.getString("schema_name");
                    log.info("Resolved tenant saccoId {} to schema: {}", saccoId, schema);
                    return schema;
                }
            }
        } catch (Exception ex) {
            log.error("Error looking up sacco schema for saccoId {}: {}", saccoId, ex.getMessage());
        }
        return TenantContext.DEFAULT_TENANT;
    }
}
