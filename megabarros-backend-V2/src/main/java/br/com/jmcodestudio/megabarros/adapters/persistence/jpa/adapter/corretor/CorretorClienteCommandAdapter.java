package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor.CorretorClienteCommandRepository;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorClienteCommandPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CorretorClienteCommandAdapter implements CorretorClienteCommandPort {

    private final CorretorClienteCommandRepository repo;

    public CorretorClienteCommandAdapter(CorretorClienteCommandRepository repo) {
        this.repo = repo;
    }

    @Override
    public Integer createLink(Integer corretorId, Integer clienteId) {
        // tenta retornar existente
        Optional<Integer> existing = repo.findId(corretorId, clienteId);
        if (existing.isPresent()) return existing.get();
        // insere idempotente e retorna id
        Integer generated = repo.insertReturningId(corretorId, clienteId);
        if (generated != null) return generated;
        // em alguns bancos, RETURNING pode não trazer quando conflito; resolva novamente
        return repo.findId(corretorId, clienteId).orElse(null);
    }

    @Override
    public Optional<Integer> findId(Integer corretorId, Integer clienteId) {
        return repo.findId(corretorId, clienteId);
    }

    @Override
    public boolean existsCorretor(Integer corretorId) {
        return repo.existsCorretor(corretorId);
    }

    @Override
    public boolean existsCliente(Integer clienteId) {
        return repo.existsCliente(clienteId);
    }
}
