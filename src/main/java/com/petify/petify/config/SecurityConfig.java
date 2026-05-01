package com.petify.petify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;


import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:5176"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users/*/pets").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/*/pets").permitAll()

                        // Public listings endpoints
                        .requestMatchers(HttpMethod.GET, "/api/listings/active").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/listings/*").permitAll()

                        // Protected listings endpoints
                        //.requestMatchers(HttpMethod.GET, "/api/listings/my-listings").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/listings").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/listings/*").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/listings/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/listings/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/listings/*/status").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/listings/*/status").permitAll()
                        .requestMatchers(HttpMethod.PATCH,"/api/users/admin/*/block").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/users/admin/*/block").permitAll()

                        // Protected user endpoints
                        .requestMatchers(HttpMethod.GET, "/api/users/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/owner/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pets/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/admin/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/analytics/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/appointments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/appointments/my").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/appointments/clinics/*/available-slots").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clinics").permitAll()

                        // Favorites endpoints - protected
                        .requestMatchers(HttpMethod.POST, "/api/favorites/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/favorites/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/favorites").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/favorites/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/listings/my-listings/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/users/*/verified").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/users/verification/top-10").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/listings/recommendations").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/users/admin/all").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/users/admin/listings").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
