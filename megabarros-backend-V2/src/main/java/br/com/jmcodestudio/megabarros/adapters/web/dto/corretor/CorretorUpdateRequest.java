package br.com.jmcodestudio.megabarros.adapters.web.dto.corretor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO para requisição de atualização de Corretor.
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
 * @param idUsuario
 */
public record CorretorUpdateRequest(
        String nomeCorretor,
        String corretora,
        String cpfCnpj,
        String susepPj,
        String susepPf,
        @Email String email,
        String telefone,
        String uf,
        LocalDate dataNascimento,
        String doc,
        Long idUsuario
) {}
