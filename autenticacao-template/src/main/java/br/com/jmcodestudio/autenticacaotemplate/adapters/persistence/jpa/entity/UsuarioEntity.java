package br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a user entity mapped to the "usuario" table in the database.
 * This class defines the database structure for storing user-related data
 * and its mapped fields.
 *
 * Annotations are used to specify the mapping between the class attributes
 * and the corresponding database columns.
 *
 * Key fields include:
 * - id: The unique identifier for a user.
 * - nome: The username, which cannot be null.
 * - email: The email address of the user, which must be unique and cannot be null.
 * - senhaHash: The hashed password of the user, which is required and cannot be null.
 * - perfil: The profile associated with the user.
 * - ativo: Whether the user is active or not, defaults to true.
 * - mustChangePassword: Whether the user needs to change their password, defaults to false.
 * - dataCriacao: The creation timestamp of the user record.
 *
 * This entity utilizes JPA annotations for ORM capabilities.
 *
 * O que fazem: mapeiam o schema do Postgres para entidades JPA.
 * Por que: adapters de persistência convertem entre entidade JPA e records dos ports.
 */

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class UsuarioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nome_usuario", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "perfil_usuario")
    private String perfil;

    @Column(name = "ativo")
    private Boolean ativo = Boolean.TRUE;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = Boolean.FALSE;

    @Column(name = "data_criacao")
    private Instant dataCriacao;

}