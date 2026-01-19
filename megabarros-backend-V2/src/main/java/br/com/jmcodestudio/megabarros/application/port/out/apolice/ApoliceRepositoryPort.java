package br.com.jmcodestudio.megabarros.application.port.out.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.*;
import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;

import java.util.List;
import java.util.Optional;

public interface ApoliceRepositoryPort {
    Apolice save(Apolice apolice);
    Optional<Apolice> findById(ApoliceId id);
    List<Apolice> findAll();
    List<Apolice> findBySeguradoraId(Integer seguradoraId);
    List<Apolice> findByProdutoId(Integer produtoId);
    List<Apolice> findByCorretorClienteId(Integer corretorClienteId);
    boolean existsByNumero(String numero);
    void deleteById(ApoliceId id);

    // Status
    ApoliceStatus addStatus(ApoliceStatus status);
    Optional<ApoliceStatus> findCurrentStatus(ApoliceId id);

    // Parcela
    Parcela saveParcela(Parcela parcela);
    List<Parcela> listParcelas(ApoliceId id);
    Optional<Parcela> findParcelaById(Integer id);
    void deleteParcelaById(Integer id);

    // Cobertura
    ApoliceCobertura saveCobertura(ApoliceCobertura cobertura);
    List<ApoliceCobertura> listCoberturas(ApoliceId id);
    void deleteCoberturaById(Integer id);

    // Beneficiário
    Beneficiario saveBeneficiario(Beneficiario beneficiario);
    List<Beneficiario> listBeneficiarios(ApoliceId id);
    void deleteBeneficiarioById(Integer id);
}
