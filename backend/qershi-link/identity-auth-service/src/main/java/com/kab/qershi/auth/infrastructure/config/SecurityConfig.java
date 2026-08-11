package com.kab.qershi.auth.infrastructure.config;

import com.kab.qershi.auth.infrastructure.security.JwtAuthenticationFilter;
import com.kab.qershi.auth.infrastructure.security.JwtTokenProvider;
import com.kab.qershi.auth.infrastructure.security.RateLimitingFilter;
import com.kab.qershi.auth.infrastructure.security.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimitingFilter rateLimitingFilter;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          RateLimitingFilter rateLimitingFilter,
                          TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.rateLimitingFilter = rateLimitingFilter;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // Stateless sessions: JWT is the single source of truth
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Explicitly permit UI documentation
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()
                        // 2. Permit core authentication entry points & global PIN operations
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register-super-admin",
                                "/api/v1/auth/change-password",
                                "/api/v1/pin/**",
                                "/api/v1/sacco/onboard"
                        ).permitAll()
                        // 3. Secure all other endpoints
                        .requestMatchers("/api/v1/platform/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Filter chain order:
                // 1. RateLimitingFilter — reject excess requests immediately (public endpoint protection)
                // 2. JwtAuthenticationFilter — validate token and check Redis blacklist on all remaining requests
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}