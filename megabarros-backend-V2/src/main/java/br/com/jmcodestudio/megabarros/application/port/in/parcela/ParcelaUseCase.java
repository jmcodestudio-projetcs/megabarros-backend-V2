package br.com.jmcodestudio.megabarros.application.port.in.parcela;

import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;
import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;

import java.util.List;
import java.util.Optional;

public interface ParcelaUseCase {
    Parcela addParcela(Parcela p);
    List<Parcela> listParcelas(ApoliceId id);
    Optional<Parcela> markPaid(Integer parcelaId);
    void deleteParcela(Integer parcelaId);
}
