package com.example.bankrest.config;

import com.example.bankrest.security.CustomUserDetailsService;
import com.example.bankrest.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 1) публичные
                        .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 2) ADMIN: CRUD пользователей
                        .requestMatchers(HttpMethod.POST,   "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/users/*/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/users/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")

                        // 3) ADMIN: CRUD карт
                        .requestMatchers(HttpMethod.GET,    "/cards").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/cards").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/cards/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/cards/*").hasRole("ADMIN")

                        // 4) USER: свои карты и переводы
                        .requestMatchers(HttpMethod.GET,    "/cards/me").hasRole("USER")
                        .requestMatchers(HttpMethod.POST,   "/cards/transfer").hasRole("USER")
                        .requestMatchers(HttpMethod.POST,   "/cards/*/block").hasRole("USER")
                        .requestMatchers(HttpMethod.GET,    "/cards/*/balance").hasRole("USER")

                        // 5) всё остальное — аутентифицировано
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
