package br.com.jmcodestudio.megabarros.application.domain.cliente;

public record Endereco(
        Integer id,
        ClienteId clienteId,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep
) {}
