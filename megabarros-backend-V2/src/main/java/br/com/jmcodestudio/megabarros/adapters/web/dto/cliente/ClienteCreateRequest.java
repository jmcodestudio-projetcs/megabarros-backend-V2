package br.com.jmcodestudio.megabarros.adapters.web.dto.cliente;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ClienteCreateRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 20) String cpfCnpj,
        @NotNull LocalDate dataNascimento,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 30) String telefone
) {}
