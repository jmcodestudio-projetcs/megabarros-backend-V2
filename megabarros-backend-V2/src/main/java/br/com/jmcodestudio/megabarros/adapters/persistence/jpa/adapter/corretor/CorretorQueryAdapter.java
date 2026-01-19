package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor.CorretorQueryRepository;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorQueryPort;
import org.springframework.stereotype.Component;

@Component
public class CorretorQueryAdapter implements CorretorQueryPort {
    private final CorretorQueryRepository repo;

    public CorretorQueryAdapter(CorretorQueryRepository repo) {
        this.repo = repo;
    }

    @Override
    public Integer findCorretorIdByUsuarioId(Long usuarioId) {
        return repo.findCorretorIdByUsuarioId(usuarioId);
    }

    @Override
    public Integer findCorretorIdByUsuarioEmail(String email) {
        return repo.findCorretorIdByUsuarioEmail(email);
    }
}
