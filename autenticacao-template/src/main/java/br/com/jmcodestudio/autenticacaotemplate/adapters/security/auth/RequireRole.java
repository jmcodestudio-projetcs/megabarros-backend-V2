package br.com.jmcodestudio.autenticacaotemplate.adapters.security.auth;

import br.com.jmcodestudio.autenticacaotemplate.application.policy.AuthorizationPolicy.Role;

import java.lang.annotation.*;

/**
 * The RequireRole annotation is used to enforce role-based access control on methods
 * or classes. It specifies the required role that a user must possess in order to access
 * the annotated method or class.
 *
 * Applying this annotation to a method or class allows for the integration of authorization
 * rules, where the system ensures that only users with the specified {@link Role} are permitted
 * to execute the associated functionality.
 *
 * This annotation is typically used in conjunction with an authorization mechanism such as
 * an aspect or policy enforcement that evaluates the user's role against the specified role.
 *
 * Attributes:
 * - value: Represents the required role for accessing the annotated element.
 *
 * Targets:
 * - Method level: Enforces role-based access for individual methods.
 * - Class level: Enforces role-based access for all methods within the class.
 *
 * Retention Policy:
 * - Runtime: This annotation is retained at runtime, allowing for runtime inspection and
 *   enforcement of role-based access control.
 *
 *   O que fazem: permitem declarar a necessidade de role(s) direto no método (controller ou use case).
 *   Onde usar: em qualquer bean gerenciado pelo Spring (controllers, @Service de use cases).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    Role value();
}