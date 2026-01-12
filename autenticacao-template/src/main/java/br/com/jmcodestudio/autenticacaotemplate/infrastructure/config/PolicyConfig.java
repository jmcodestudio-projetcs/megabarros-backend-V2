package br.com.jmcodestudio.autenticacaotemplate.infrastructure.config;

import br.com.jmcodestudio.autenticacaotemplate.application.policy.PasswordPolicy;
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
 * Note: authorizationPolicy bean is defined in AopConfig as it's specific to AOP configuration.
 */
@Configuration
public class PolicyConfig {

    @Bean
    public PasswordPolicy passwordPolicy() { return new PasswordPolicy(); }
}