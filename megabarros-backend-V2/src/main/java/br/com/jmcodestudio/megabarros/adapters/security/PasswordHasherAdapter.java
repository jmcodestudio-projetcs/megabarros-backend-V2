package br.com.jmcodestudio.megabarros.adapters.security;

import br.com.jmcodestudio.megabarros.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapter implementation of the PasswordHasherPort interface.
 * This class provides methods for hashing passwords and verifying password hashes using the BCrypt algorithm.
 * It acts as a bridge between the application's core use cases and the BCrypt library, enabling flexibility in changing
 * the hashing algorithm without affecting the use cases.
 *
 * O que faz: implementa hash de senhas com BCrypt.
 * Por que: algoritmo forte com salt embutido.
 */
@Component
public class PasswordHasherAdapter implements PasswordHasherPort {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String raw) {
        return encoder.encode(raw);
    }

    @Override
    public boolean matches(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}