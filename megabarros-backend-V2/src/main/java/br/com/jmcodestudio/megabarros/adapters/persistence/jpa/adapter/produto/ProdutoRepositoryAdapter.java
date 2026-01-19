package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.produto;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.produto.ProdutoEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper.ProdutoPersistenceMapper;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.produto.ProdutoJpaRepository;
import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import br.com.jmcodestudio.megabarros.application.port.out.produto.ProdutoRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class ProdutoRepositoryAdapter implements ProdutoRepositoryPort {

    private final ProdutoJpaRepository jpa;
    private final ProdutoPersistenceMapper mapper;

    public ProdutoRepositoryAdapter(ProdutoJpaRepository jpa, ProdutoPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Produto save(Produto produto) {
        ProdutoEntity e = mapper.toEntity(produto);
        ProdutoEntity s = jpa.save(e);
        return mapper.toDomain(s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> findBySeguradoraId(SeguradoraId seguradoraId) {
        return jpa.findBySeguradora_Id(seguradoraId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> findById(ProdutoId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public void deleteById(ProdutoId id) {
        jpa.deleteById(id.value());
    }
}