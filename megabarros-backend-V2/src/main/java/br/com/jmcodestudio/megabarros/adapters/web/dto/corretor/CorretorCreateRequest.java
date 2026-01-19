package br.com.jmcodestudio.megabarros.adapters.web.dto.corretor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO para requisição de criação de Corretor.
 * @param idUsuario
 * @param nomeCorretor
 * @param corretora
 * @param cpfCnpj
 * @param susepPj
 * @param susepPf
 * @param email
 * @param telefone
 * @param uf
 * @param dataNascimento
 * @param doc
 */
public record CorretorCreateRequest(
        Long idUsuario,
        @NotBlank @Size(max = 150) String nomeCorretor,
        @Size(max = 150) String corretora,
        @Size(max = 18) String cpfCnpj,
        @Size(max = 50) String susepPj,
        @Size(max = 50) String susepPf,
        @Email @Size(max = 150) String email,
        @Size(max = 20) String telefone,
        @Size(max = 2) String uf,
        LocalDate dataNascimento,
        @Size(max = 1000) String doc
) {}
