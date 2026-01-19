package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ApoliceStatusJpaRepository extends JpaRepository<ApoliceStatusEntity, Integer> {
    @Query("select s from ApoliceStatusEntity s where s.idApolice = :idApolice and s.dataFim is null order by s.dataInicio desc")
    Optional<ApoliceStatusEntity> findCurrent(Integer idApolice);
}
