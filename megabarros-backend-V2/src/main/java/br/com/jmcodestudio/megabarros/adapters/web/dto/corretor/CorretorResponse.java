package br.com.jmcodestudio.megabarros.adapters.web.dto.corretor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para resposta de Corretor.
 * @param idCorretor
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
 * @param dataCriacao
 */
public record CorretorResponse(
        Integer idCorretor,
        Long idUsuario,
        String nomeCorretor,
        String corretora,
        String cpfCnpj,
        String susepPj,
        String susepPf,
        String email,
        String telefone,
        String uf,
        LocalDate dataNascimento,
        String doc,
        LocalDateTime dataCriacao
) {}
