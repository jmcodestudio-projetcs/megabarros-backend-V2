package br.com.jmcodestudio.autenticacaotemplate.application.policy;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * The PasswordPolicy class enforces a set of rules for validating password strength.
 * It ensures that passwords meet specific criteria such as minimum length, inclusion
 * of various character types, and exclusion of predefined blocked passwords.
 *
 * The policy rules include:
 * - Minimum password length requirement.
 * - At least one uppercase letter in the password.
 * - At least one lowercase letter in the password.
 * - At least one numeric character in the password.
 * - At least one special character in the password.
 * - Restriction on using passwords from a blocked list.
 *
 * Methods:
 * - validateOrThrow(String password): Validates a password against the policy rules.
 *   Throws a WeakPasswordException if the password does not comply with the policy.
 *
 * Inner Classes:
 * - WeakPasswordException: Represents an exception that is thrown when a password
 *   does not meet the defined strength requirements.
 *
 * This class is immutable and designed for use in scenarios where robust password
 * strength validation is necessary to ensure security.
 *
 * Centraliza a validação de complexidade mínima de senha para consistência e fácil manutenção.
 * Valida complexidade mínima da senha; é um ponto único de regra.
 */
public class PasswordPolicy {

    private final int minLength;
    private final Pattern upper = Pattern.compile(".*[A-Z].*");
    private final Pattern lower = Pattern.compile(".*[a-z].*");
    private final Pattern digit = Pattern.compile(".*\\d.*");
    private final Pattern special = Pattern.compile(".*[^A-Za-z0-9].*");
    private final Set<String> blocked;

    public PasswordPolicy() {
        this.minLength = 12;
        this.blocked = Set.of("Password123!", "Admin123!", "1234567890!", "Qwerty123!", "Senha@123");
    }

    public void validateOrThrow(String password) {
        if (password == null || password.length() < minLength) {
            throw new WeakPasswordException("Password too short (min " + minLength + ")");
        }
        if (!upper.matcher(password).matches()) throw new WeakPasswordException("Password needs uppercase");
        if (!lower.matcher(password).matches()) throw new WeakPasswordException("Password needs lowercase");
        if (!digit.matcher(password).matches()) throw new WeakPasswordException("Password needs digit");
        if (!special.matcher(password).matches()) throw new WeakPasswordException("Password needs special char");
        if (blocked.contains(password)) throw new WeakPasswordException("Password is in blocked list");
    }

    public static class WeakPasswordException extends RuntimeException {
        public WeakPasswordException(String msg) { super(msg); }
    }
}