package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "apolice_status")
@Getter
@Setter
public class ApoliceStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status")
    private Integer id;

    @Column(name = "id_apolice", nullable = false)
    private Integer idApolice;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;
}
