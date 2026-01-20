package br.com.jmcodestudio.megabarros.application.port.in.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.Apolice;
import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;

import java.util.List;
import java.util.Optional;

public interface ListApolicesUseCase {
    List<Apolice> listAll();
    List<Apolice> listBySeguradora(Integer seguradoraId);
    List<Apolice> listByProduto(Integer produtoId);
    List<Apolice> listByCorretorCliente(Integer corretorClienteId);
    Optional<Apolice> getById(ApoliceId id);
}
