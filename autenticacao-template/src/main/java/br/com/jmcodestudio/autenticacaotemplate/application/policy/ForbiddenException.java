package br.com.jmcodestudio.autenticacaotemplate.application.policy;

/**
 * Represents an exception that is thrown to indicate that an operation is forbidden
 * due to insufficient permissions or lack of required access roles.
 *
 * This exception is typically used in scenarios where role-based access control is enforced
 * and the calling user does not meet the appropriate authorization requirements.
 *
 * It is a runtime exception that allows for handling or propagating specific access control
 * violations within the application.
 *
 * O que faz: representa “acesso negado” segundo a política de autorização do domínio/aplicação.
 * Onde usar: lançada pelo AuthorizationPolicy e tratada no handler global como 403.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}