package br.com.jmcodestudio.megabarros.application.port.in.usuario;

import br.com.jmcodestudio.megabarros.application.port.out.UsuarioRepositoryPort;

import java.util.Optional;

public interface GetUsuarioUseCase {
    Optional<UsuarioRepositoryPort.UsuarioRecord> getById(Long id);
}
