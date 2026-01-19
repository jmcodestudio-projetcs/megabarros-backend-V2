package br.com.jmcodestudio.megabarros.application.domain.cliente;

import java.time.LocalDate;
import java.util.List;

public record Cliente(
        ClienteId id,
        String nome,
        String cpfCnpj,
        LocalDate dataNascimento,
        String email,
        String telefone,
        Boolean ativo,
        List<Endereco> enderecos
) {}
