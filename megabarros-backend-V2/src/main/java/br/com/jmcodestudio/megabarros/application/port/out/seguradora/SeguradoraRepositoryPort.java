package br.com.jmcodestudio.megabarros.application.port.out.seguradora;

import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;

import java.util.List;
import java.util.Optional;

public interface SeguradoraRepositoryPort {
    Seguradora save(Seguradora seguradora);
    Optional<Seguradora> findById(SeguradoraId id);
    List<Seguradora> findAll();
    void deleteById(SeguradoraId id);
}
