package br.com.jmcodestudio.megabarros.application.port.in.seguradora;

import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;

import java.util.List;

public interface ListSeguradorasUseCase {
    List<Seguradora> listAll();
}
