package br.com.jmcodestudio.megabarros.application.port.in.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;
import br.com.jmcodestudio.megabarros.application.domain.apolice.Beneficiario;

import java.util.List;

public interface BeneficiarioUseCase {
    Beneficiario addBeneficiario(Beneficiario b);
    List<Beneficiario> listBeneficiarios(ApoliceId id);
    void deleteBeneficiario(Integer id);
}
