package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.seguradora;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.seguradora.SeguradoraEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper.SeguradoraPersistenceMapper;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.seguradora.SeguradoraJpaRepository;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import br.com.jmcodestudio.megabarros.application.port.out.seguradora.SeguradoraRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class SeguradoraRepositoryAdapter implements SeguradoraRepositoryPort {

    private final SeguradoraJpaRepository jpa;
    private final SeguradoraPersistenceMapper mapper;

    public SeguradoraRepositoryAdapter(SeguradoraJpaRepository jpa, SeguradoraPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Seguradora save(Seguradora seguradora) {
        SeguradoraEntity e = mapper.toEntity(seguradora);
        SeguradoraEntity s = jpa.save(e);
        return mapper.toDomain(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seguradora> findById(SeguradoraId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seguradora> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(SeguradoraId id) {
        jpa.deleteById(id.value());
    }
}
