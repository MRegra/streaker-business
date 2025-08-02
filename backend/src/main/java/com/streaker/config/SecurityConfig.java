package com.streaker.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final Environment environment;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JwtAuthFilter is a Spring-managed singleton bean and is not mutated.")
    public SecurityConfig(Environment environment, JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.environment = environment;
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .headers(headers -> {
                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny); // X-Frame-Options: DENY
                    headers.contentSecurityPolicy(csp -> csp
                            .policyDirectives("default-src 'self'")
                    );
                    if (!isDev()) {
                        headers.httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        );
                    }
                })
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/users/login", "/api/v1/users/register").permitAll();
                    auth.requestMatchers("/api/actuator/health", "/actuator/health").permitAll();
                    auth.requestMatchers("/api/v1/users/**").authenticated();

                    if (isDev()) {
                        auth.anyRequest().permitAll();
                    } else {
                        auth.requestMatchers(HttpMethod.POST, "/v1/users/login", "/v1/users/register").permitAll();
                        auth.requestMatchers("/v1/users/**").authenticated();
                        auth.anyRequest().denyAll();
                    }
                })
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, ex) -> {
                            log.warn("ACCESS DENIED: [{} {}] from {}",
                                    request.getMethod(),
                                    request.getRequestURI(),
                                    request.getRemoteAddr()
                            );
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        })
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private boolean isDev() {
        return environment.acceptsProfiles(Profiles.of("dev", "test"));
    }
}
