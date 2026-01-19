package br.com.jmcodestudio.megabarros.application.port.in.seguradora;

import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;

public interface CreateSeguradoraUseCase {
    Seguradora create(Seguradora seguradora);
}
