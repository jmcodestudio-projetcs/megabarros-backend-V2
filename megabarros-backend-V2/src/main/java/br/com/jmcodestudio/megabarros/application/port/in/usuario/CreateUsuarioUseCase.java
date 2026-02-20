package br.com.jmcodestudio.megabarros.application.port.in.usuario;

import br.com.jmcodestudio.megabarros.application.port.out.UsuarioRepositoryPort;

public interface CreateUsuarioUseCase {
    UsuarioRepositoryPort.UsuarioRecord create(UsuarioCreateCommand command);

    record UsuarioCreateCommand(
            String nome,
            String email,
            String senha,
            String perfil,
            Boolean ativo,
            Boolean mustChangePassword
    ) {}
}
