package com.kab.qershi.loan.origination.infrastructure.config;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filter processing JWT authentication tokens and extracting multi-tenant schema headers.
 * Resolves saccoId -> schema_name via master_schema.sacco_registry table lookup.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final JdbcTemplate jdbcTemplate;

    public JwtAuthenticationFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String tenantHeader = request.getHeader("X-Tenant-Schema");

        String resolvedSchema = null;

        if (tenantHeader != null && !tenantHeader.isBlank()) {
            resolvedSchema = tenantHeader.trim();
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                String userIdStr = claims.get("userId", String.class);
                String saccoIdStr = claims.get("saccoId", String.class);

                List<String> authorities = claims.get("authorities", List.class);
                List<SimpleGrantedAuthority> grantedAuthorities = authorities != null
                        ? authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                        : Collections.emptyList();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                if (resolvedSchema == null && saccoIdStr != null && !saccoIdStr.isBlank()) {
                    resolvedSchema = resolveSchemaBySaccoId(UUID.fromString(saccoIdStr));
                }

            } catch (Exception ex) {
                log.warn("Invalid JWT Token in request to {}: {}", request.getRequestURI(), ex.getMessage());
            }
        }

        if (resolvedSchema != null) {
            TenantContext.setTenantSchema(resolvedSchema);
            log.trace("TenantContext set to schema: {}", resolvedSchema);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveSchemaBySaccoId(UUID saccoId) {
        try {
            String sql = "SELECT schema_name FROM master_schema.sacco_registry WHERE sacco_id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, saccoId);
        } catch (Exception ex) {
            log.warn("Failed resolving schema for saccoId {}: {}", saccoId, ex.getMessage());
            return null;
        }
    }
}
