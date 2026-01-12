package br.com.jmcodestudio.autenticacaotemplate.application.port.in;

/**
 * Interface representing the use case for changing user passwords.
 * O que faz: Define o contrato do caso de uso de mudar senha.
 * Por que: Troca senha (valida senha atual, grava nova e revoga refresh tokens).
 */
public interface ChangePasswordUseCase {
    void changePassword(Long userId, String currentPassword, String newPassword);
}
