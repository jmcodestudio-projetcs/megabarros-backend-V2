package br.com.jmcodestudio.megabarros.application.domain.corretor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Corretor represents a broker in the system. *
 * @param id
 * @param usuarioId
 * @param nome
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
public record Corretor(
        CorretorId id,
        Long usuarioId,
        String nome,
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
) {
    public Corretor withId(CorretorId newId) {
        return new Corretor(newId, usuarioId, nome, corretora, cpfCnpj, susepPj, susepPf, email, telefone, uf, dataNascimento, doc, dataCriacao);
    }
}
