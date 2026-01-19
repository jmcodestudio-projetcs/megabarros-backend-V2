package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.parcela;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "parcela_apolice",
        uniqueConstraints = @UniqueConstraint(name = "uq_parcela_numero_por_apolice", columnNames = {"id_apolice", "numero_parcela"}))
@Getter
@Setter
public class ParcelaApoliceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parcela")
    private Integer id;

    @Column(name = "id_apolice", nullable = false)
    private Integer idApolice;

    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "valor_parcela", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorParcela;

    @Column(name = "status_pagamento", length = 50)
    private String statusPagamento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;
}
