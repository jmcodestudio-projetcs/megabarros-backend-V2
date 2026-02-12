package br.com.jmcodestudio.megabarros.application.port.out.corretor;

import java.util.Optional;

public interface CorretorClienteCommandPort {
    // Cria (idempotente) e retorna o id_corretor_cliente
    Integer createLink(Integer corretorId, Integer clienteId);

    // Resolve id existente
    Optional<Integer> findId(Integer corretorId, Integer clienteId);

    // Verificações simples de existência (opcional, para 404 amigável)
    boolean existsCorretor(Integer corretorId);
    boolean existsCliente(Integer clienteId);
}
