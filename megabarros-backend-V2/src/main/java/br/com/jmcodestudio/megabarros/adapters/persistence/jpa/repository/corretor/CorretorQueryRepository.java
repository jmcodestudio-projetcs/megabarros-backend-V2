package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CorretorQueryRepository extends Repository<ClienteEntity, Integer> {

    @Query(value = "SELECT id_corretor FROM corretor WHERE id_usuario = :userId", nativeQuery = true)
    Integer findCorretorIdByUsuarioId(@Param("userId") Long userId);

    @Query(value = "SELECT c.id_corretor " +
            "FROM corretor c JOIN usuario u ON u.id_usuario = c.id_usuario " +
            "WHERE u.email = :email", nativeQuery = true)
    Integer findCorretorIdByUsuarioEmail(@Param("email") String email);
}