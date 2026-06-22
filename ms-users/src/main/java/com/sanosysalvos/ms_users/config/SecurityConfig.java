package com.sanosysalvos.ms_users.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

@Value("${bff.cors.allowed-origins}")
private String[] allowedOrigins; // Spring convierte la coma en un Array automáticamente

@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Ahora usamos la variable en lugar de un texto fijo
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
    
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true); // Recomendado si manejas sesiones o cookies

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
    .requestMatchers("/v3/api-docs", "/v3/api-docs/**",
                     "/swagger-ui.html", "/swagger-ui/**", "/webjars/**").permitAll()
    .requestMatchers("/api/users", "/api/users/**").permitAll() // ← agregar
    .anyRequest().authenticated()
)
        .oauth2ResourceServer(oauth2 -> 
            oauth2.jwt(jwt -> {}));
    
    return http.build();
}
}
