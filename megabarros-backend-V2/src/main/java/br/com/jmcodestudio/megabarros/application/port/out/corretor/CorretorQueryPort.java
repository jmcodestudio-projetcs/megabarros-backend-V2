package br.com.jmcodestudio.megabarros.application.port.out.corretor;

public interface CorretorQueryPort {
    Integer findCorretorIdByUsuarioId(Long usuarioId);
    Integer findCorretorIdByUsuarioEmail(String email);
}