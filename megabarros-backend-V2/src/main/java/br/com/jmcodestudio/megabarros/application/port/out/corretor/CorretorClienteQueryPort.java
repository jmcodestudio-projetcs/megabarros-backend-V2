package br.com.jmcodestudio.megabarros.application.port.out.corretor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CorretorClienteQueryPort {

    boolean existsByCorretorIdAndClienteId(Integer corretorId, Integer clienteId);

    List<CorretorClienteLink> listByClienteId(Integer clienteId);

    Optional<Integer> findIdByCorretorIdAndClienteId(Integer corretorId, Integer clienteId);

    record CorretorClienteLink(
            Integer idCorretorCliente,
            Integer idCorretor,
            Integer idCliente,
            String nomeCorretor,
            String uf,
            String email,
            LocalDate dataInicio
    ) {}
}
