package br.com.jmcodestudio.megabarros.application.port.in.usuario;

import br.com.jmcodestudio.megabarros.application.port.out.UsuarioRepositoryPort;

import java.util.Optional;

public interface UpdateUsuarioUseCase {
    Optional<UsuarioRepositoryPort.UsuarioRecord> update(Long id, UsuarioUpdateCommand command);

    record UsuarioUpdateCommand(
            String nome,
            String email,
            String senha,
            String perfil,
            Boolean ativo,
            Boolean mustChangePassword
    ) {}
}