package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor.CorretorClienteQueryRepository;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorClienteQueryPort;
import org.springframework.stereotype.Component;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor.CorretorClienteQueryRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class CorretorClienteQueryAdapter implements CorretorClienteQueryPort {

    private final CorretorClienteQueryRepository repo;

    public CorretorClienteQueryAdapter(CorretorClienteQueryRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean existsByCorretorIdAndClienteId(Integer corretorId, Integer clienteId) {
        return repo.existsByCorretorIdAndClienteId(corretorId, clienteId);
    }

    @Override
    public List<CorretorClienteLink> listByClienteId(Integer clienteId) {
        return repo.listByClienteId(clienteId).stream()
                .map(r -> new CorretorClienteLink(
                        r.getIdCorretorCliente(),
                        r.getIdCorretor(),
                        r.getIdCliente(),
                        r.getNomeCorretor(),
                        r.getUf(),
                        r.getEmail(),
                        r.getDataInicio() != null ? r.getDataInicio().toLocalDate() : null
                ))
                .toList();
    }

    @Override
    public Optional<Integer> findIdByCorretorIdAndClienteId(Integer corretorId, Integer clienteId) {
        return repo.findIdByCorretorIdAndClienteId(corretorId, clienteId);
    }
}