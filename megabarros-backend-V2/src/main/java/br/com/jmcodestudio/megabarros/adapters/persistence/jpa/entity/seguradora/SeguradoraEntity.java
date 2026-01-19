package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.seguradora;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seguradora")
@Getter
@Setter
public class SeguradoraEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguradora")
    private Integer id;

    @Column(name = "nome_seguradora", nullable = false, unique = true, length = 100)
    private String nome;
}
