package br.com.jmcodestudio.megabarros.adapters.web.support;

import br.com.jmcodestudio.megabarros.application.port.out.TokenServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class JwtTestUtils {

    @Autowired
    private TokenServicePort tokens;

    public String adminToken(Long userId, String email) {
        return tokens.generateAccessToken(userId, email, "ADMIN", Map.of(), Instant.now());
    }

    public String usuarioToken(Long userId, String email) {
        return tokens.generateAccessToken(userId, email, "USUARIO", Map.of(), Instant.now());
    }

    public String corretorToken(Long userId, String email) {
        return tokens.generateAccessToken(userId, email, "CORRETOR", Map.of(), Instant.now());
    }

    public String authHeader(String token) {
        return "Bearer " + token;
    }
}
