package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice.AggregatedApoliceQueryRepository;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice.ApoliceExistenceRepository;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class ApoliceQueryAdapter implements ApoliceQueryPort {

    private final ApoliceExistenceRepository repo;
    private final AggregatedApoliceQueryRepository aggRepo;

    public ApoliceQueryAdapter(ApoliceExistenceRepository existenceRepo,
                               AggregatedApoliceQueryRepository aggRepo) {
        this.repo = existenceRepo;
        this.aggRepo = aggRepo;
    }

    @Override
    public boolean existsBySeguradoraId(SeguradoraId seguradoraId) {
        return repo.existsBySeguradoraId(seguradoraId.value());
    }

    @Override
    public boolean existsByProdutoId(ProdutoId produtoId) {
        return repo.existsByProdutoId(produtoId.value());
    }

    @Override
    public long countBySeguradoraId(SeguradoraId seguradoraId) {
        return repo.countBySeguradoraId(seguradoraId.value());
    }

    @Override
    public long countByProdutoId(ProdutoId produtoId) {
        return repo.countByProdutoId(produtoId.value());
    }

    @Override
    public boolean existsActiveByClienteId(Integer clienteId) {
        return repo.existsActiveByClienteId(clienteId);
    }

    @Override
    public Map<Integer, Long> countBySeguradoraIds(List<SeguradoraId> ids) {
        List<Integer> raw = ids.stream().map(SeguradoraId::value).toList();
        return aggRepo.countBySeguradoraIds(raw).stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Override
    public Map<Integer, Long> countByProdutoIds(List<ProdutoId> ids) {
        List<Integer> raw = ids.stream().map(ProdutoId::value).toList();
        return aggRepo.countByProdutoIds(raw).stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }
}
