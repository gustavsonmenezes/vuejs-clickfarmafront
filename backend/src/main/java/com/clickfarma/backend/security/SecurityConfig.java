package com.clickfarma.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/telegram/webhook").permitAll()
                        .requestMatchers("/api/whatsapp/webhook").permitAll()
                        .requestMatchers("/api/simulacao/**").permitAll()
                        .requestMatchers("/api/rastreios/**").permitAll()
                        .requestMatchers("/api/pedidos/*/rastreio/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email/teste/*").permitAll()
                        .requestMatchers("/api/produtos/**").permitAll()
                        .requestMatchers("/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rastreios/**").permitAll()
                        .requestMatchers("/api/usuarios").permitAll()
                        .requestMatchers("/api/gemini/**").permitAll()
                        .requestMatchers("/api/receita/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/dashboard-saude/**").permitAll()
                        .requestMatchers("/api/recompra/**").permitAll()
                        .requestMatchers("/api/cep/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/entregadores/cadastro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/entregadores/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/entregadores/com-localizacao").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/entregadores/disponiveis").permitAll()
                        .requestMatchers("/api/entregadores/**").authenticated()
                        .requestMatchers("/api/entregas-entregador/**").authenticated()
                        .requestMatchers("/api/weather/**").permitAll()
                        .requestMatchers("/api/farmacias/**").permitAll()
                        .requestMatchers("/api/cupons/**").permitAll()
                        .requestMatchers("/api/admin/farmacias/**").authenticated()
                        .requestMatchers("/api/corridas/**").authenticated()
                        .requestMatchers("/api/admin/corridas/**").authenticated()
                        .requestMatchers("/api/avaliacoes/**").authenticated()
                        .requestMatchers("/ws-corridas/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
