package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER")
                .build();        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    @Profile("dev")
    public SecurityFilterChain devFilterChain(HttpSecurity http, 
                                         JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/h2-console/**")
                    .disable())                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())) // Allow H2 console frames
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))                .authorizeHttpRequests(authz -> authz
                        // Allow authentication endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        
                        // Allow centralized JWT endpoints
                        .requestMatchers("/api/v1/jwt/**").permitAll()
                        
                        // Allow H2 console
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Allow Swagger UI and API docs
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        
                        // Allow Actuator endpoints (all for development)
                        .requestMatchers("/actuator/**").permitAll()
                        
                        // Allow error page and root endpoint
                        .requestMatchers("/error", "/", "/favicon.ico").permitAll()
                        
                        // Allow static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        
                        // Allow demo endpoints without authentication for testing
                        .requestMatchers("/api/demo/**").permitAll()

                        // Student endpoints with role-based access
                        .requestMatchers(HttpMethod.GET, "/api/v1/student/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/student/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/student/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/student/**").hasRole("ADMIN")
                        
                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain prodFilterChain(HttpSecurity http, 
                                      JwtAuthenticationFilter jwtAuthenticationFilter,
                                      @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled) throws Exception {
        
        HttpSecurity httpSecurity = http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
          if (h2ConsoleEnabled) {
            httpSecurity
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/jwt/**").permitAll()
                    .requestMatchers("/h2-console/**").hasRole("ADMIN") // Restrict H2 console to admins only
                    .requestMatchers("/actuator/health", "/actuator/info", "/actuator/metrics").permitAll()
                    .requestMatchers("/error", "/", "/favicon.ico").permitAll()
                    .requestMatchers("/api/demo/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/student/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/student/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/student/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/student/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                );
        } else {
            httpSecurity                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/jwt/**").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info", "/actuator/metrics").permitAll()
                    .requestMatchers("/error", "/", "/favicon.ico").permitAll()
                    .requestMatchers("/api/demo/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/student/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/student/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/student/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/student/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                );
        }
        
        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return httpSecurity.build();
    }
}