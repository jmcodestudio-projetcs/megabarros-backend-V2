package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.produto;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.seguradora.SeguradoraEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "produto",
        uniqueConstraints = @UniqueConstraint(name = "uq_produto_nome_por_seguradora",
                columnNames = {"id_seguradora", "nome_produto"}))
@Getter
@Setter
public class ProdutoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_seguradora", nullable = false)
    private SeguradoraEntity seguradora;

    @Column(name = "nome_produto", nullable = false, length = 100)
    private String nome;

    @Column(name = "tipo_produto", length = 50)
    private String tipo;
}
