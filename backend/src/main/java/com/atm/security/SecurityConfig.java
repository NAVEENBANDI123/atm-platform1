package com.atm.security;

import com.atm.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomUserDetailsService userDetailsService;
    private final AppProperties appProperties;

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/customer/**",
            "/api/v1/auth/employee/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()

                        // SUPER_ADMIN - employee management & full system access
                        .requestMatchers("/api/v1/admin/employees/**")
                            .hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers("/api/v1/admin/audit/**")
                            .hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // SUPER_ADMIN + ACCOUNTANT - customer onboarding approvals
                        .requestMatchers("/api/v1/admin/customers/**")
                            .hasAnyRole("SUPER_ADMIN", "ADMIN", "ACCOUNTANT")

                        // SUPER_ADMIN + CASHIER - cash counter operations
                        .requestMatchers("/api/v1/cashier/**")
                            .hasAnyRole("SUPER_ADMIN", "ADMIN", "CASHIER")

                        // Card workflow: customer applies, officer reviews, super_admin approves
                        .requestMatchers("/api/v1/cards/applications/*/review")
                            .hasAnyRole("CARD_OFFICER", "SUPER_ADMIN", "ADMIN")
                        .requestMatchers("/api/v1/cards/applications/*/approve",
                                         "/api/v1/cards/applications/*/reject")
                            .hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers("/api/v1/cards/applications/pending")
                            .hasAnyRole("CARD_OFFICER", "SUPER_ADMIN", "ADMIN")

                        // Loan workflow
                        .requestMatchers("/api/v1/loans/applications/*/review")
                            .hasAnyRole("LOAN_OFFICER", "SUPER_ADMIN", "ADMIN")
                        .requestMatchers("/api/v1/loans/applications/*/approve",
                                         "/api/v1/loans/applications/*/reject")
                            .hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers("/api/v1/loans/applications/pending")
                            .hasAnyRole("LOAN_OFFICER", "SUPER_ADMIN", "ADMIN")

                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(appProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
