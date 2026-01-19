package br.com.jmcodestudio.megabarros.application.port.out.corretor;

public interface CorretorClienteQueryPort {
    boolean existsByCorretorIdAndClienteId(Integer corretorId, Integer clienteId);
}
