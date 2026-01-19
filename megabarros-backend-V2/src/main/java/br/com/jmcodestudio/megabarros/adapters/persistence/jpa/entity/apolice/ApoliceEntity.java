package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "apolice",
        uniqueConstraints = @UniqueConstraint(name = "uq_apolice_numero", columnNames = "numero_apolice"))
@Getter
@Setter
public class ApoliceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_apolice")
    private Integer id;

    @Column(name = "numero_apolice", nullable = false, length = 50)
    private String numero;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fim", nullable = false)
    private LocalDate vigenciaFim;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "comissao_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal comissaoPercentual;

    @Column(name = "tipo_contrato", nullable = false, length = 50)
    private String tipoContrato;

    @Column(name = "id_corretor_cliente", nullable = false)
    private Integer idCorretorCliente;

    @Column(name = "id_produto", nullable = false)
    private Integer idProduto;

    @Column(name = "id_seguradora", nullable = false)
    private Integer idSeguradora;
}
