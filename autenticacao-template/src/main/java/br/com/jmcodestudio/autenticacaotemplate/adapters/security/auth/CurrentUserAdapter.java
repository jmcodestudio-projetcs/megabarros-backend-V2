package br.com.jmcodestudio.autenticacaotemplate.adapters.security.auth;

import br.com.jmcodestudio.autenticacaotemplate.application.port.out.CurrentUserPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Adapter implementation of the {@code CurrentUserPort} interface that provides
 * access to details of the currently authenticated user within the application's
 * security context.
 *
 * Responsibilities:
 * - Retrieves the authenticated user's ID, email, and role from the security context.
 * - Extracts user information from the authentication token populated by the {@code JwtAuthenticationFilter}.
 *
 * Key Behaviors:
 * - The method {@code userId()} retrieves the user ID of the authenticated user.
 * - The method {@code email()} retrieves the email address of the authenticated user.
 * - The method {@code role()} retrieves the role (without the prefix "ROLE_") of the authenticated user.
 *
 * Use Cases:
 * - Enables application components to access the context of the current user without direct dependency
 *   on specific authentication or security frameworks.
 * - Facilitates role-based authorization and user-specific functionality by providing user context.
 *
 * Implementation Notes:
 * - Dependent on the {@code JwtAuthenticationFilter} to populate the security context with a
 *   {@code Principal} object that contains user details.
 * - Handles situations where the security context or authenticated principal is null, returning null
 *   in such cases to avoid assumptions about user authentication.
 *
 *   O que faz: implementa CurrentUserPort lendo SecurityContextHolder. Extrai a primeira authority “ROLE_XXX” e remove o prefixo.
 *   Onde usar: pelo AuthorizationAspect.
 */
@Component
public class CurrentUserAdapter implements CurrentUserPort {

    @Override
    public Long userId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        if (auth.getPrincipal() instanceof br.com.jmcodestudio.autenticacaotemplate.adapters.security.jwt.JwtAuthenticationFilter.Principal p) {
            return p.userId();
        }
        return null;
    }

    @Override
    public String email() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        if (auth.getPrincipal() instanceof br.com.jmcodestudio.autenticacaotemplate.adapters.security.jwt.JwtAuthenticationFilter.Principal p) {
            return p.email();
        }
        return null;
    }

    @Override
    public String role() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return null;
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a != null && a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .orElse(null);
    }
}