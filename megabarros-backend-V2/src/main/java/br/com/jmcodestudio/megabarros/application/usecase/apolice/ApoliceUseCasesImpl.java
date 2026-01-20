package br.com.jmcodestudio.megabarros.application.usecase.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.*;
import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;
import br.com.jmcodestudio.megabarros.application.port.in.apolice.*;
import br.com.jmcodestudio.megabarros.application.port.in.parcela.ParcelaUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApoliceUseCasesImpl implements
        CreateApoliceUseCase, UpdateApoliceUseCase, ListApolicesUseCase, CancelApoliceUseCase,
        ParcelaUseCase, BeneficiarioUseCase, CoberturaUseCase {

    private static final Logger log = LoggerFactory.getLogger(ApoliceUseCasesImpl.class);

    private final ApoliceRepositoryPort repo;
    private final CurrentUserPort currentUser;

    public ApoliceUseCasesImpl(ApoliceRepositoryPort repo, CurrentUserPort currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    private void ensureNotCorretor() {
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            log.warn("apolice.permission denied role={}", role);
            throw new AccessDeniedException("Corretores não podem alterar apólices.");
        }
    }

    private void validateVigencia(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Vigência fim deve ser maior ou igual à vigência início.");
        }
    }

    @Override
    public Apolice create(Apolice apolice) {
        String actor = currentUser.username();
        String role = currentUser.role();
        log.info("apolice.create start actor={} role={} numero={}", actor, role, apolice.numeroApolice());

        ensureNotCorretor();
        if (apolice.numeroApolice() != null && repo.existsByNumero(apolice.numeroApolice())) {
            log.warn("apolice.create conflict duplicate-num actor={} numero={}", actor, apolice.numeroApolice());
            throw new IllegalStateException("Número de apólice já existente: " + apolice.numeroApolice());
        }
        validateVigencia(apolice.vigenciaInicio(), apolice.vigenciaFim());

        Apolice saved = repo.save(apolice);
        repo.addStatus(new ApoliceStatus(null, saved.id(), "ATIVA", LocalDateTime.now(), null));
        log.info("apolice.create success actor={} id={} status=ATIVA", actor, saved.id().value());
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
    @Transactional(readOnly = true)
    public Optional<Apolice> getById(ApoliceId id) {
        return repo.findById(id);
    }

    @Override
    public Optional<Apolice> update(ApoliceId id, Apolice updates) {
        String actor = currentUser.username();
        log.info("apolice.update start actor={} id={}", actor, id.value());
        ensureNotCorretor();
        return repo.findById(id).map(existing -> {
            String novoNumero = updates.numeroApolice() != null ? updates.numeroApolice() : existing.numeroApolice();
            if (!novoNumero.equals(existing.numeroApolice()) && repo.existsByNumero(novoNumero)) {
                log.warn("apolice.update conflict duplicate-num actor={} id={} numero={}", actor, id.value(), novoNumero);
                throw new IllegalStateException("Número de apólice já existente: " + novoNumero);
            }
            var inicio = updates.vigenciaInicio() != null ? updates.vigenciaInicio() : existing.vigenciaInicio();
            var fim = updates.vigenciaFim() != null ? updates.vigenciaFim() : existing.vigenciaFim();
            validateVigencia(inicio, fim);

            Apolice merged = new Apolice(
                    existing.id(), novoNumero,
                    updates.dataEmissao() != null ? updates.dataEmissao() : existing.dataEmissao(),
                    inicio, fim,
                    updates.valor() != null ? updates.valor() : existing.valor(),
                    updates.comissaoPercentual() != null ? updates.comissaoPercentual() : existing.comissaoPercentual(),
                    updates.tipoContrato() != null ? updates.tipoContrato() : existing.tipoContrato(),
                    updates.idCorretorCliente() != null ? updates.idCorretorCliente() : existing.idCorretorCliente(),
                    updates.idProduto() != null ? updates.idProduto() : existing.idProduto(),
                    updates.idSeguradora() != null ? updates.idSeguradora() : existing.idSeguradora(),
                    existing.statusAtual(), existing.parcelas(), existing.coberturas(), existing.beneficiarios()
            );
            Apolice saved = repo.save(merged);
            log.info("apolice.update success actor={} id={}", actor, id.value());
            return saved;
        });
    }

    @Override
    public void cancel(ApoliceId id, String reason) {
        String actor = currentUser.username();
        log.info("apolice.cancel start actor={} id={} reason={}", actor, id.value(), reason);
        ensureNotCorretor();

        Optional<ApoliceStatus> current = repo.findCurrentStatus(id);
        current.ifPresent(s -> {
            log.info("apolice.cancel closing-status id={} status={} start={}", id.value(), s.status(), s.dataInicio());
            repo.addStatus(new ApoliceStatus(s.id(), id, s.status(), s.dataInicio(), LocalDateTime.now()));
        });

        repo.addStatus(new ApoliceStatus(null, id, "CANCELADA", LocalDateTime.now(), null));
        log.info("apolice.cancel success id={} newStatus=CANCELADA", id.value());
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
