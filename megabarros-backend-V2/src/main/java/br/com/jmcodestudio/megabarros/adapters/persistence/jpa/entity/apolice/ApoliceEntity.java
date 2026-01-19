package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice;

import jakarta.persistence.*;

@Entity
@Table(name = "apolice")
public class ApoliceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_apolice")
    private Integer id;

    @Column(name = "id_seguradora")
    private Integer seguradoraId;

    @Column(name = "id_produto")
    private Integer produtoId;

    // getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSeguradoraId() { return seguradoraId; }
    public void setSeguradoraId(Integer seguradoraId) { this.seguradoraId = seguradoraId; }

    public Integer getProdutoId() { return produtoId; }
    public void setProdutoId(Integer produtoId) { this.produtoId = produtoId; }
}
