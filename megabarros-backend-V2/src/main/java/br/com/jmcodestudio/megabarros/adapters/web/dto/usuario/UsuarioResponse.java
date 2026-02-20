package br.com.jmcodestudio.megabarros.adapters.web.dto.usuario;

public record UsuarioResponse(
        Long idUsuario,
        String nome,
        String email,
        String perfil,
        boolean ativo,
        boolean mustChangePassword
) {}
