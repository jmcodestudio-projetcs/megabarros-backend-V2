package br.com.jmcodestudio.megabarros.application.usecase.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.*;
import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;
import br.com.jmcodestudio.megabarros.application.port.in.apolice.*;
import br.com.jmcodestudio.megabarros.application.port.in.parcela.ParcelaUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceRepositoryPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApoliceUseCasesImpl implements
        CreateApoliceUseCase, UpdateApoliceUseCase, ListApolicesUseCase, CancelApoliceUseCase,
        ParcelaUseCase, BeneficiarioUseCase, CoberturaUseCase {

    private final ApoliceRepositoryPort repo;
    private final CurrentUserPort currentUser;

    public ApoliceUseCasesImpl(ApoliceRepositoryPort repo, CurrentUserPort currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    private void ensureNotCorretor() {
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            throw new AccessDeniedException("Corretores não podem alterar apólices.");
        }
    }

    @Override
    public Apolice create(Apolice apolice) {
        ensureNotCorretor();
        if (apolice.numeroApolice() != null && repo.existsByNumero(apolice.numeroApolice())) {
            throw new IllegalStateException("Número de apólice já existente: " + apolice.numeroApolice());
        }
        // status inicial
        Apolice saved = repo.save(apolice);
        repo.addStatus(new ApoliceStatus(null, saved.id(), "ATIVA", LocalDateTime.now(), null));
        return repo.findById(saved.id()).orElse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apolice> listAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apolice> listBySeguradora(Integer seguradoraId) {
        return repo.findBySeguradoraId(seguradoraId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apolice> listByProduto(Integer produtoId) {
        return repo.findByProdutoId(produtoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apolice> listByCorretorCliente(Integer corretorClienteId) {
        return repo.findByCorretorClienteId(corretorClienteId);
    }

    @Override
    public Optional<Apolice> update(ApoliceId id, Apolice updates) {
        ensureNotCorretor();
        return repo.findById(id).map(existing -> {
            Apolice merged = new Apolice(
                    existing.id(),
                    updates.numeroApolice() != null ? updates.numeroApolice() : existing.numeroApolice(),
                    updates.dataEmissao() != null ? updates.dataEmissao() : existing.dataEmissao(),
                    updates.vigenciaInicio() != null ? updates.vigenciaInicio() : existing.vigenciaInicio(),
                    updates.vigenciaFim() != null ? updates.vigenciaFim() : existing.vigenciaFim(),
                    updates.valor() != null ? updates.valor() : existing.valor(),
                    updates.comissaoPercentual() != null ? updates.comissaoPercentual() : existing.comissaoPercentual(),
                    updates.tipoContrato() != null ? updates.tipoContrato() : existing.tipoContrato(),
                    updates.idCorretorCliente() != null ? updates.idCorretorCliente() : existing.idCorretorCliente(),
                    updates.idProduto() != null ? updates.idProduto() : existing.idProduto(),
                    updates.idSeguradora() != null ? updates.idSeguradora() : existing.idSeguradora(),
                    existing.statusAtual(),
                    existing.parcelas(),
                    existing.coberturas(),
                    existing.beneficiarios()
            );
            return repo.save(merged);
        });
    }

    @Override
    public void cancel(ApoliceId id, String reason) {
        ensureNotCorretor();
        // fecha status atual e cria CANCELADA
        Optional<ApoliceStatus> current = repo.findCurrentStatus(id);
        current.ifPresent(s -> repo.addStatus(new ApoliceStatus(s.id(), id, s.status(), s.dataInicio(), LocalDateTime.now())));
        repo.addStatus(new ApoliceStatus(null, id, "CANCELADA", LocalDateTime.now(), null));
    }

    // Parcela
    @Override
    public Parcela addParcela(Parcela p) {
        ensureNotCorretor();
        return repo.saveParcela(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parcela> listParcelas(ApoliceId id) {
        return repo.listParcelas(id);
    }

    @Override
    public Optional<Parcela> markPaid(Integer parcelaId) {
        ensureNotCorretor();
        return repo.findParcelaById(parcelaId).map(existing -> {
            Parcela paid = new Parcela(
                    existing.id(),
                    existing.apoliceId(),
                    existing.numeroParcela(),
                    existing.dataVencimento(),
                    existing.valorParcela(),
                    "PAGA",
                    java.time.LocalDate.now()
            );
            repo.saveParcela(paid);
            return paid;
        });
    }

    @Override
    public void deleteParcela(Integer parcelaId) {
        ensureNotCorretor();
        repo.deleteParcelaById(parcelaId);
    }

    // Beneficiário
    @Override
    public Beneficiario addBeneficiario(Beneficiario b) {
        ensureNotCorretor();
        return repo.saveBeneficiario(b);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Beneficiario> listBeneficiarios(ApoliceId id) {
        return repo.listBeneficiarios(id);
    }

    @Override
    public void deleteBeneficiario(Integer id) {
        ensureNotCorretor();
        repo.deleteBeneficiarioById(id);
    }

    // Cobertura
    @Override
    public ApoliceCobertura addCobertura(ApoliceCobertura c) {
        ensureNotCorretor();
        return repo.saveCobertura(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApoliceCobertura> listCoberturas(ApoliceId id) {
        return repo.listCoberturas(id);
    }

    @Override
    public void deleteCobertura(Integer id) {
        ensureNotCorretor();
        repo.deleteCoberturaById(id);
    }
}
