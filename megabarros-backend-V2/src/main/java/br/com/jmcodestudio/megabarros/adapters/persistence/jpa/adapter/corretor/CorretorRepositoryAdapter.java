package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.corretor.CorretorEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper.CorretorPersistenceMapper;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor.CorretorJpaRepository;
import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;
import br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adapter de repositório para a entidade Corretor, implementando a porta de repositório.
 */
@Component
@Transactional
public class CorretorRepositoryAdapter implements CorretorRepositoryPort {

    private final CorretorJpaRepository jpa;
    private final CorretorPersistenceMapper mapper;

    public CorretorRepositoryAdapter(CorretorJpaRepository jpa, CorretorPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Corretor save(Corretor corretor) {
        CorretorEntity entity = mapper.toEntity(corretor);
        CorretorEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Corretor> findById(CorretorId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Corretor> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Corretor> findByUsuarioId(Long usuarioId) {
        return jpa.findByUsuarioId(usuarioId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsuarioId(Long usuarioId) {
        return jpa.existsByUsuarioId(usuarioId);
    }

    @Override
    public boolean deleteById(CorretorId id) {
        if (jpa.existsById(id.value())) {
            jpa.deleteById(id.value());
            return true;
        }
        return false;
    }
}