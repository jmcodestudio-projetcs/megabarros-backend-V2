package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "beneficiario")
@Getter
@Setter
public class BeneficiarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficiario")
    private Integer id;

    @Column(name = "id_apolice", nullable = false)
    private Integer idApolice;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "nome_beneficiario", nullable = false, length = 150)
    private String nomeBeneficiario;

    @Column(name = "cpf", length = 14)
    private String cpf;

    @Column(name = "percentual_participacao", precision = 5, scale = 2)
    private BigDecimal percentualParticipacao;
}
