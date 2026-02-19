package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor.CorretorClienteCommandRepository;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorClienteCommandPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class CorretorClienteCommandAdapter implements CorretorClienteCommandPort {

    private final CorretorClienteCommandRepository repo;

    public CorretorClienteCommandAdapter(CorretorClienteCommandRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Integer createLink(Integer corretorId, Integer clienteId) {
        // 1) tenta retornar existente
        Optional<Integer> existing = repo.findId(corretorId, clienteId);
        if (existing.isPresent()) return existing.get();

        // 2) insere idempotente (sem RETURNING)
        repo.insertIgnore(corretorId, clienteId);

        // 3) resolve novamente o id
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

    @Override
    public boolean existsLink(Integer corretorClienteId) {
        return repo.existsLink(corretorClienteId);
    }

    @Override
    @Transactional
    public void deleteLink(Integer corretorClienteId) {
        repo.deleteById(corretorClienteId);
    }
}