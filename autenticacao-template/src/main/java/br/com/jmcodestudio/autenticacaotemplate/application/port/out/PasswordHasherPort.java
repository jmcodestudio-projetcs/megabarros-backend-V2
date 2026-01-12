package br.com.jmcodestudio.autenticacaotemplate.application.port.out;

/**
 * Interface representing the port for password hashing operations.
 * Define o contrato do port para hash de senha.
 * Abstrai o hash de senhas (BCrypt).
 * Por que: permite trocar o algoritmo sem tocar nos use cases.
 */
public interface PasswordHasherPort {
    String hash(String raw);
    boolean matches(String raw, String hash);
}
