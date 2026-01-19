package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceStatusEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.parcela.ParcelaApoliceEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper.ApolicePersistenceMapper;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice.*;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.parcela.ParcelaApoliceJpaRepository;
import br.com.jmcodestudio.megabarros.application.domain.apolice.*;

import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class ApoliceRepositoryAdapter implements ApoliceRepositoryPort {

    private final ApoliceJpaRepository apoliceRepo;
    private final ApoliceStatusJpaRepository statusRepo;
    private final ParcelaApoliceJpaRepository parcelaRepo;
    // Mantemos os repos abaixo para futuro, mas não usamos nesta fase:
    private final ApoliceCoberturaJpaRepository coberturaRepo;
    private final BeneficiarioJpaRepository beneficiarioRepo;
    private final ApolicePersistenceMapper mapper;

    public ApoliceRepositoryAdapter(ApoliceJpaRepository apoliceRepo,
                                    ApoliceStatusJpaRepository statusRepo,
                                    ParcelaApoliceJpaRepository parcelaRepo,
                                    ApoliceCoberturaJpaRepository coberturaRepo,
                                    BeneficiarioJpaRepository beneficiarioRepo,
                                    ApolicePersistenceMapper mapper) {
        this.apoliceRepo = apoliceRepo;
        this.statusRepo = statusRepo;
        this.parcelaRepo = parcelaRepo;
        this.coberturaRepo = coberturaRepo;
        this.beneficiarioRepo = beneficiarioRepo;
        this.mapper = mapper;
    }

    private Apolice hydrate(ApoliceEntity e) {
        String statusAtual = statusRepo.findCurrent(e.getId()).map(ApoliceStatusEntity::getStatus).orElse(null);
        List<Parcela> parcelas = parcelaRepo.findByIdApolice(e.getId()).stream().map(mapper::toDomain).toList();
        // Nesta fase, não carregamos coberturas e beneficiários
        return mapper.toDomain(e, statusAtual, parcelas, List.of(), List.of());
    }

    @Override
    public Apolice save(Apolice apolice) {
        ApoliceEntity e = mapper.toEntity(apolice);
        ApoliceEntity s = apoliceRepo.save(e);
        return hydrate(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Apolice> findById(ApoliceId id) {
        return apoliceRepo.findById(id.value()).map(this::hydrate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apolice> findAll() {
        return apoliceRepo.findAll().stream().map(this::hydrate).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apolice> findBySeguradoraId(Integer seguradoraId) {
        return apoliceRepo.findByIdSeguradora(seguradoraId).stream().map(this::hydrate).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apolice> findByProdutoId(Integer produtoId) {
        return apoliceRepo.findByIdProduto(produtoId).stream().map(this::hydrate).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apolice> findByCorretorClienteId(Integer corretorClienteId) {
        return apoliceRepo.findByIdCorretorCliente(corretorClienteId).stream().map(this::hydrate).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNumero(String numero) {
        return apoliceRepo.existsByNumero(numero);
    }

    @Override
    public void deleteById(ApoliceId id) {
        apoliceRepo.deleteById(id.value());
    }

    @Override
    public ApoliceStatus addStatus(ApoliceStatus status) {
        ApoliceStatusEntity e = mapper.toEntity(status);
        ApoliceStatusEntity s = statusRepo.save(e);
        return mapper.toDomain(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApoliceStatus> findCurrentStatus(ApoliceId id) {
        return statusRepo.findCurrent(id.value()).map(mapper::toDomain);
    }

    // Parcela
    @Override
    public Parcela saveParcela(Parcela p) {
        ParcelaApoliceEntity e = mapper.toEntity(p);
        ParcelaApoliceEntity s = parcelaRepo.save(e);
        return mapper.toDomain(s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parcela> listParcelas(ApoliceId id) {
        return parcelaRepo.findByIdApolice(id.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Parcela> findParcelaById(Integer id) {
        return parcelaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteParcelaById(Integer id) {
        parcelaRepo.deleteById(id);
    }

    // Cobertura e Beneficiário: métodos permanecem, mas não serão usados nesta fase.
    @Override
    public ApoliceCobertura saveCobertura(ApoliceCobertura c) {
        throw new UnsupportedOperationException("Cobertura não suportada nesta fase");
    }

    @Override
    public List<ApoliceCobertura> listCoberturas(ApoliceId id) {
        return List.of();
    }

    @Override
    public void deleteCoberturaById(Integer id) {
        throw new UnsupportedOperationException("Cobertura não suportada nesta fase");
    }

    @Override
    public Beneficiario saveBeneficiario(Beneficiario b) {
        throw new UnsupportedOperationException("Beneficiário não suportado nesta fase");
    }

    @Override
    public List<Beneficiario> listBeneficiarios(ApoliceId id) {
        return List.of();
    }

    @Override
    public void deleteBeneficiarioById(Integer id) {
        throw new UnsupportedOperationException("Beneficiário não suportado nesta fase");
    }
}