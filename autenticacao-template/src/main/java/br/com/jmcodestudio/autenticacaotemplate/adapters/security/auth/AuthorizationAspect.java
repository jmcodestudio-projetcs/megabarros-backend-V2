package br.com.jmcodestudio.autenticacaotemplate.adapters.security.auth;

import br.com.jmcodestudio.autenticacaotemplate.application.policy.AuthorizationPolicy;
import br.com.jmcodestudio.autenticacaotemplate.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.autenticacaotemplate.application.policy.AuthorizationPolicy.Role;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * The AuthorizationAspect class is responsible for enforcing role-based access control
 * using aspect-oriented programming (AOP). It provides mechanisms to validate the roles
 * of the currently authenticated user against the required roles defined using
 * {@link RequireRole} and {@link RequireAnyRole} annotations on methods or classes.
 *
 * The aspect intercepts method calls annotated with these annotations and ensures that
 * the current user has the necessary roles to access the functionality.
 *
 * Responsibilities:
 * - Intercepts methods or classes annotated with {@link RequireRole} or {@link RequireAnyRole}.
 * - Validates the user's role against the required role(s) defined in the annotations.
 * - Throws an exception if the user does not have the necessary role(s) to access the target.
 *
 * Dependencies:
 * - The {@link AuthorizationPolicy} component is used to implement role validation logic.
 * - The {@link CurrentUserPort} interface provides the role of the currently authenticated user.
 *
 * Methods:
 * - checkSingleRole(JoinPoint jp, RequireRole requireRole):
 *   Validates that the user possesses the single required role specified by the {@link RequireRole} annotation.
 *   The method handles both class-level and method-level annotations.
 *
 * - checkAnyRole(JoinPoint jp, RequireAnyRole requireAnyRole):
 *   Validates that the user possesses at least one of the roles specified by the {@link RequireAnyRole} annotation.
 *   The method handles both class-level and method-level annotations.
 *
 * Usage:
 * This class is used as a Spring component and is automatically applied to the target methods
 * and classes within the application through AspectJ pointcuts.
 *
 * O que faz: antes de métodos anotados com @RequireRole ou @RequireAnyRole, consulta o CurrentUserPort e aplica a AuthorizationPolicy.
 * Onde usar: automaticamente em qualquer bean Spring.
 */
@Aspect
@Component
public class AuthorizationAspect {

    private final AuthorizationPolicy policy;
    private final CurrentUserPort current;

    public AuthorizationAspect(AuthorizationPolicy policy, CurrentUserPort current) {
        this.policy = policy;
        this.current = current;
    }

    @Before("@within(requireRole) || @annotation(requireRole)")
    public void checkSingleRole(JoinPoint jp, RequireRole requireRole) {
        // Quando anotado na classe, requireRole pode vir nulo aqui; tratar @within e @annotation separadamente:
        if (requireRole == null) {
            var annotation = jp.getTarget().getClass().getAnnotation(RequireRole.class);
            if (annotation != null) {
                requireRole = annotation;
            }
        }
        if (requireRole != null) {
            String role = current.role();
            Role expected = requireRole.value();
            policy.requireRole(role, expected);
        }
    }

    @Before("@within(requireAnyRole) || @annotation(requireAnyRole)")
    public void checkAnyRole(JoinPoint jp, RequireAnyRole requireAnyRole) {
        if (requireAnyRole == null) {
            var annotation = jp.getTarget().getClass().getAnnotation(RequireAnyRole.class);
            if (annotation != null) {
                requireAnyRole = annotation;
            }
        }
        if (requireAnyRole != null) {
            String role = current.role();
            var expected = Set.of(requireAnyRole.value());
            policy.requireAnyRole(role, expected);
        }
    }
}