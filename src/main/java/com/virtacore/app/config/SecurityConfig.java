package com.virtacore.app.config;


import com.virtacore.app.exception.dto.ErrorResponse;
import com.virtacore.app.security.JwtAuthFilter;
import com.virtacore.app.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Endpoints public
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex

                        // 401
                        .authenticationEntryPoint((req, res, authEx) -> {

                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");

                            ErrorResponse error = ErrorResponse.builder()
                                    .success(false)
                                    .status(401)
                                    .message("Authentication required or token missing/invalid")
                                    .path(req.getRequestURI())
                                    .timestamp(LocalDateTime.now())
                                    .build();

                            res.getWriter().write(objectMapper.writeValueAsString(error));
                        })

                        // 403
                        .accessDeniedHandler((req, res,e) -> {

                            res.setStatus(403);
                            res.setContentType("application/json;charset=UTF-8");

                            ErrorResponse error = ErrorResponse.builder()
                                    .success(false)
                                    .status(403)
                                    .message("You don't have permission")
                                    .path(req.getRequestURI())
                                    .timestamp(LocalDateTime.now())
                                    .build();

                            res.getWriter().write(objectMapper.writeValueAsString(error));
                        })
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
