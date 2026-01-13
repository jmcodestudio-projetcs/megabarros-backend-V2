package br.com.jmcodestudio.megabarros.application.policy;

import java.util.Set;

/**
 * The AuthorizationPolicy class provides methods to manage role-based access control
 * by verifying user roles against expected roles and sets of roles. It allows for determining
 * or enforcing whether a role meets specific access requirements.
 *
 * O que faz: centraliza a regra de autorização por role.
 * Onde usar: chamada pelo Aspecto de autorização.
 */
public class AuthorizationPolicy {

    public enum Role { ADMIN, CORRETOR, USUARIO }

    public boolean hasRole(String role, Role expected) {
        if (role == null) return false;
        return roleEquals(role, expected);
    }

    public void requireRole(String role, Role expected) {
        if (!hasRole(role, expected)) {
            throw new ForbiddenException("Required role: " + expected);
        }
    }

    public boolean anyRole(String role, Set<Role> roles) {
        if (role == null) return false;
        return roles.stream().anyMatch(r -> roleEquals(role, r));
    }

    public void requireAnyRole(String role, Set<Role> roles) {
        if (!anyRole(role, roles)) {
            throw new ForbiddenException("Required any role in: " + roles);
        }
    }

    private boolean roleEquals(String roleStr, Role expected) {
        return roleStr.equalsIgnoreCase(expected.name());
    }
}