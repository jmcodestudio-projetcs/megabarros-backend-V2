package br.com.jmcodestudio.megabarros.application.port.in.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceCobertura;
import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;

import java.util.List;

public interface CoberturaUseCase {
    ApoliceCobertura addCobertura(ApoliceCobertura c);
    List<ApoliceCobertura> listCoberturas(ApoliceId id);
    void deleteCobertura(Integer id);
}
