package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.corretor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class representing a Corretor (Broker) in the database.
 */
@Entity
@Table(name = "corretor")
@Getter
@Setter
public class CorretorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_corretor")
    private Integer id;

    @Column(name = "id_usuario")
    private Long usuarioId;

    @Column(name = "nome_corretor", nullable = false, length = 150)
    private String nome;

    @Column(name = "corretora", length = 150)
    private String corretora;

    @Column(name = "cpf_cnpj", length = 18)
    private String cpfCnpj;

    @Column(name = "susep_pj", length = 50)
    private String susepPj;

    @Column(name = "susep_pf", length = 50)
    private String susepPf;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf", columnDefinition = "char(2)")
    private String uf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "doc", length = 1000)
    private String doc;

    @CreationTimestamp
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;
}