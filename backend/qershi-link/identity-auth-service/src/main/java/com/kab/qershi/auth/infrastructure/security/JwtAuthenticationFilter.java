package com.kab.qershi.auth.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                // 1. Parse and validate the token
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtTokenProvider.getKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String msisdn = claims.getSubject();
                String saccoId = claims.get("saccoId", String.class);
                String userId = claims.get("userId", String.class);

                // 2. Extract authorities (permissions/roles) from the token claims
                List<String> authorities = claims.get("authorities", List.class);

                if (msisdn != null && authorities != null) {
                    // 3. Map strings to Spring Security GrantedAuthority objects
                    List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // 4. Set the authentication with authorities included
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(msisdn, null, grantedAuthorities);

                    // Attach tenant context details (saccoId, userId) to Authentication
                    Map<String, String> details = new HashMap<>();
                    if (saccoId != null) details.put("saccoId", saccoId);
                    if (userId != null) details.put("userId", userId);
                    auth.setDetails(details);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Token is invalid/expired - clear context to be safe
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}