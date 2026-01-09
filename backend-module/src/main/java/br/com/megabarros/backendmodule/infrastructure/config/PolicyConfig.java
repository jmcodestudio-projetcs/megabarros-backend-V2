package br.com.megabarros.backendmodule.infrastructure.config;

import br.com.megabarros.backendmodule.application.policy.PasswordPolicy;
import br.com.megabarros.backendmodule.application.policy.AuthorizationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that defines and exposes policies related to application security
 * as Spring beans. This allows dependency injection of these policies wherever needed.
 *
 * Annotations:
 * - @Configuration: Declares this class as a Spring configuration class, enabling the
 *   Spring container to process it and generate Spring beans.
 *
 * Beans:
 * - passwordPolicy: Provides an instance of the PasswordPolicy, responsible for
 *   enforcing password strength rules such as minimum length, required character
 *   types, and blocked passwords.
 *
 * - authorizationPolicy: Provides an instance of the AuthorizationPolicy, responsible
 *   for managing role-based access control logic across the application.
 *
 *   Registrar como bean
 */
@Configuration
public class PolicyConfig {

    @Bean
    public PasswordPolicy passwordPolicy() { return new PasswordPolicy(); }

    @Bean
    public AuthorizationPolicy authorizationPolicy() { return new AuthorizationPolicy(); }
}