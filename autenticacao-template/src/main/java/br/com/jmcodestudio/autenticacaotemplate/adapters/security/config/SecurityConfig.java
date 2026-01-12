package br.com.jmcodestudio.autenticacaotemplate.adapters.security.config;

import br.com.jmcodestudio.autenticacaotemplate.adapters.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for securing the application using Spring Security.
 * This class defines the security filter chain for managing HTTP security.
 *
 * O que faz: configura o Spring Security, libera /auth/** e exige autenticação no resto; instala o filtro JWT.
 * Por que: separa configuração de segurança da lógica de tokens.
 *
 * Somente /actuator/health é público; os demais exigem ADMIN
 * Restringe actuator; instala filtro de JWT.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}