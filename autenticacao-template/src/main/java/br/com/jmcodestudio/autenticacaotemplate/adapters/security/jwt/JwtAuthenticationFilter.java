package br.com.jmcodestudio.autenticacaotemplate.adapters.security.jwt;

import br.com.jmcodestudio.autenticacaotemplate.application.port.out.TokenServicePort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * A filter used for handling JWT-based authentication in incoming HTTP requests.
 * This class extends the Spring Security {@code OncePerRequestFilter} to ensure
 * execution once per request within a single request processing lifecycle.
 *
 * The filter extracts the JWT token from the "Authorization" header, validates it,
 * and loads the user details into the Spring Security context if the token is valid.
 *
 * Constructor Details:
 * - Accepts a {@code TokenServicePort} implementation for handling token validation and parsing.
 *
 * Key Behaviors:
 * - Parses the "Authorization" header to extract and validate the JWT token.
 * - Delegates token parsing and verification to the provided {@code TokenServicePort}.
 * - Loads user details such as user ID, email, and role from the token claims.
 * - Populates the security context with an {@code UsernamePasswordAuthenticationToken}
 *   if the token is successfully validated.
 * - Clears the security context in case of token validation failure.
 *
 * Internal Classes:
 * - {@code Principal}: A record used to encapsulate user information (user ID and email)
 *   extracted from the JWT claims for authentication purposes.
 *
 * Method Overrides:
 * - {@code doFilterInternal}: Processes the HTTP request and performs
 *   the described JWT-based authentication. This method also delegates
 *   further request processing to the next filter in the chain.
 *
 * Dependencies:
 * - {@code TokenServicePort}: Used for verifying and extracting claims from the JWT.
 * - {@code UsernamePasswordAuthenticationToken}: Represents authentication with authorities.
 * - {@code SimpleGrantedAuthority}: Converts roles to Spring Security authority objects.
 *
 * O que faz: intercepta requisições, lê “Authorization: Bearer …”, valida access token, e popula o SecurityContext com um Principal simples contendo userId e email.
 * Por que: disponibiliza o usuário autenticado para os controllers, sem acoplar ao core.
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenServicePort tokens;

    public JwtAuthenticationFilter(TokenServicePort tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            var token = header.substring(7);
            try {
                var claims = tokens.parseAndValidateAccess(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        new Principal(claims.userId(), claims.email()),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + String.valueOf(claims.role()).toUpperCase()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    public record Principal(Long userId, String email) {}
}