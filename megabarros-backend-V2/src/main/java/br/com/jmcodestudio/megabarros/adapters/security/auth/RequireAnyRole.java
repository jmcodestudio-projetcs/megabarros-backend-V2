package br.com.jmcodestudio.megabarros.adapters.security.auth;

import br.com.jmcodestudio.megabarros.application.policy.AuthorizationPolicy.Role;

import java.lang.annotation.*;

/**
 * The RequireAnyRole annotation is used to enforce access control based on a set of roles
 * a user may have. It is applied to methods or classes to specify multiple roles, any of which
 * the user must possess to access the annotated element.
 *
 * By utilizing this annotation, the application ensures that a user has at least one of the
 * specified roles before executing the associated functionality. It is helpful in scenarios
 * where different roles are permitted to access the same resource or feature.
 *
 * Attributes:
 * - value: Represents the array of roles, any one of which is sufficient for authorization.
 *
 * Targets:
 * - Method level: Restricts method access based on roles.
 * - Class level: Restricts access to all methods within the class.
 *
 * Retention Policy:
 * - Runtime: The annotation is retained at runtime, allowing for runtime inspection and
 *   enforcement of authorization policies.
 *
 * Typical usage involves integrating this annotation with an authorization mechanism that
 * validates the user's role against the specified roles.
 *
 * O que fazem: permitem declarar a necessidade de role(s) direto no método (controller ou use case).
 * Onde usar: em qualquer bean gerenciado pelo Spring (controllers, @Service de use cases).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAnyRole {
    Role[] value();
}