package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "apolice_cobertura",
        uniqueConstraints = @UniqueConstraint(name = "uq_ac_por_apolice_cobertura", columnNames = {"id_apolice", "id_cobertura"}))
@Getter
@Setter
public class ApoliceCoberturaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_apolice_cobertura")
    private Integer id;

    @Column(name = "id_apolice", nullable = false)
    private Integer idApolice;

    @Column(name = "id_cobertura", nullable = false)
    private Integer idCobertura;

    @Column(name = "valor_contratado", precision = 12, scale = 2)
    private BigDecimal valorContratado;
}
