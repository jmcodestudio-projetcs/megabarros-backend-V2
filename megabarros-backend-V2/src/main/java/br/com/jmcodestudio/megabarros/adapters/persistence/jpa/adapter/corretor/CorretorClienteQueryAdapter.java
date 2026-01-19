package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor.CorretorClienteQueryRepository;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorClienteQueryPort;
import org.springframework.stereotype.Component;

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
}