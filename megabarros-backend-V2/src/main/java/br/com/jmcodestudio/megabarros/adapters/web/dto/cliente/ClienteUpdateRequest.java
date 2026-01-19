package br.com.jmcodestudio.megabarros.adapters.web.dto.cliente;

import java.time.LocalDate;

public record ClienteUpdateRequest(
        String nome,
        String cpfCnpj,
        LocalDate dataNascimento,
        String email,
        String telefone
) {}
