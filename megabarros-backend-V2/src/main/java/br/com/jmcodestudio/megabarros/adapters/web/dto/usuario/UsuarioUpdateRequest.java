package br.com.jmcodestudio.megabarros.adapters.web.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @Size(max = 100) String nome,
        @Email @Size(max = 150) String email,
        @Size(min = 12, max = 100) String senha,
        @Size(max = 50) String perfil,
        Boolean ativo,
        Boolean mustChangePassword
) {}