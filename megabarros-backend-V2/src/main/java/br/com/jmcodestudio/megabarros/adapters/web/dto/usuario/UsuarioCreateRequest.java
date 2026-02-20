package br.com.jmcodestudio.megabarros.adapters.web.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 12, max = 100) String senha,
        @NotBlank @Size(max = 50) String perfil,
        Boolean ativo,
        Boolean mustChangePassword
) {}
