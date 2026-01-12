package br.com.jmcodestudio.autenticacaotemplate.infrastructure.config;

import br.com.jmcodestudio.autenticacaotemplate.application.policy.AuthorizationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration class for setting up Aspect-Oriented Programming (AOP) in the application.
 * This configures Spring's AOP infrastructure and enables the use of aspects to manage
 * cross-cutting concerns such as authorization.
 *
 * An instance of the AuthorizationPolicy class is defined as a Spring bean to centralize
 * role-based access control logic. This policy supports role verification and enforcement
 * for different application requirements.
 *
 * Annotations:
 * - @Configuration: Indicates that this class contains Spring bean definitions.
 * - @EnableAspectJAutoProxy: Enables support for handling components marked with @Aspect
 *   in the application context.
 *
 * Beans:
 * - authorizationPolicy: Provides a central authorization utility for enforcing
 *   role-based access control throughout the application.
 *
 *   O que faz: liga o suporte a AspectJ proxy no Spring e disponibiliza a AuthorizationPolicy para injeção no aspecto.
 */
@Configuration
@EnableAspectJAutoProxy
public class AopConfig {

    @Bean
    public AuthorizationPolicy authorizationPolicy() {
        return new AuthorizationPolicy();
    }
}