package br.com.jmcodestudio.megabarros.application.port.in.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.Apolice;

import java.util.List;

public interface ListApolicesUseCase {
    List<Apolice> listAll();
    List<Apolice> listBySeguradora(Integer seguradoraId);
    List<Apolice> listByProduto(Integer produtoId);
    List<Apolice> listByCorretorCliente(Integer corretorClienteId);
}
