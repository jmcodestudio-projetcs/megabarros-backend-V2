package br.com.jmcodestudio.megabarros.adapters.web.dto.corretorCliente;

import java.time.LocalDate;

public record CorretorClienteResponse(
        Integer idCorretorCliente,
        Integer idCorretor,
        Integer idCliente,
        String nomeCorretor,
        String uf,
        String email,
        LocalDate dataInicio
) {}
