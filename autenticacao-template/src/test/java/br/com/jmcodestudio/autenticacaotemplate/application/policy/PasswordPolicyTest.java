package br.com.jmcodestudio.autenticacaotemplate.application.policy;

import br.com.jmcodestudio.autenticacaotemplate.application.policy.PasswordPolicy.WeakPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @Test
    void rejectsTooShort() {
        var p = new PasswordPolicy();
        assertThrows(WeakPasswordException.class, () -> p.validateOrThrow("A1!a"));
    }

    @Test
    void rejectsNoUppercase() {
        var p = new PasswordPolicy();
        assertThrows(WeakPasswordException.class, () -> p.validateOrThrow("lowercase1!lower"));
    }

    @Test
    void rejectsNoLowercase() {
        var p = new PasswordPolicy();
        assertThrows(WeakPasswordException.class, () -> p.validateOrThrow("UPPERCASE1!UPPER"));
    }

    @Test
    void rejectsNoDigit() {
        var p = new PasswordPolicy();
        assertThrows(WeakPasswordException.class, () -> p.validateOrThrow("NoDigits!!!!aA"));
    }

    @Test
    void rejectsNoSpecial() {
        var p = new PasswordPolicy();
        assertThrows(WeakPasswordException.class, () -> p.validateOrThrow("NoSpecials123aA"));
    }

    @Test
    void rejectsBlockedList() {
        var p = new PasswordPolicy();
        assertThrows(WeakPasswordException.class, () -> p.validateOrThrow("Senha@123"));
    }

    @Test
    void acceptsStrongPassword() {
        var p = new PasswordPolicy();
        assertDoesNotThrow(() -> p.validateOrThrow("StrongPass@2025"));
    }
}