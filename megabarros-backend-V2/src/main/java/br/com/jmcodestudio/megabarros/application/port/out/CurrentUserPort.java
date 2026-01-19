package br.com.jmcodestudio.megabarros.application.port.out;

/**
 * Interface representing the access point for information about the currently authenticated user.
 * Provides methods to retrieve user-specific details, such as ID, email, and role, in the context of an ongoing request or session.
 *
 * Usage:
 * - Decouples core business logic from specific authentication or session frameworks.
 * - Facilitates obtaining user context during the execution of use cases.
 * - Supports authorization and user-specific operations by exposing relevant user details.
 *
 * O que faz: abstrai o acesso às informações do usuário atual (id, email, role), evitando acoplamento do core ao Spring Security.
 * Onde usar: o Aspecto de autorização usa este port para descobrir a role do usuário.
 */
public interface CurrentUserPort {
    Long userId();
    String email();
    String role(); // ADMIN, CORRETOR, USUARIO (sem prefixo ROLE_)
    String username();
}