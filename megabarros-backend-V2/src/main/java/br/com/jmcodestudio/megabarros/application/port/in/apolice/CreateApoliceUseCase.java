package br.com.jmcodestudio.megabarros.application.port.in.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.Apolice;

public interface CreateApoliceUseCase {
    Apolice create(Apolice apolice);
}
