package br.com.jmcodestudio.megabarros.application.port.in.usuario;

import br.com.jmcodestudio.megabarros.application.port.out.UsuarioRepositoryPort;

import java.util.List;

public interface ListUsuariosUseCase {
    List<UsuarioRepositoryPort.UsuarioRecord> listAll();
}
