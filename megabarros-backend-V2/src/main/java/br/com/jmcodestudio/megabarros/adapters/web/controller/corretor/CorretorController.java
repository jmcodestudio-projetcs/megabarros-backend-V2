package br.com.jmcodestudio.megabarros.adapters.web.controller.corretor;

import br.com.jmcodestudio.megabarros.adapters.web.dto.corretor.CorretorCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.corretor.CorretorResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.corretor.CorretorUpdateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.corretor.CorretorWebMapper;
import br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId;
import br.com.jmcodestudio.megabarros.application.port.in.corretor.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/corretores")
@CrossOrigin(origins = "http://localhost:5173")
public class CorretorController {

    private final CreateCorretorUseCase createUC;
    private final UpdateCorretorUseCase updateUC;
    private final DeleteCorretorUseCase deleteUC;
    private final GetCorretorUseCase getUC;
    private final GetCurrentCorretorUseCase getCurrentUC;
    private final CorretorWebMapper webMapper;

    public CorretorController(CreateCorretorUseCase createUC,
                              UpdateCorretorUseCase updateUC,
                              DeleteCorretorUseCase deleteUC,
                              GetCorretorUseCase getUC,
                              GetCurrentCorretorUseCase getCurrentUC,
                              CorretorWebMapper webMapper) {
        this.createUC = createUC;
        this.updateUC = updateUC;
        this.deleteUC = deleteUC;
        this.getUC = getUC;
        this.getCurrentUC = getCurrentUC;
        this.webMapper = webMapper;
    }

    @GetMapping
    public ResponseEntity<List<CorretorResponse>> listar() {
        var list = getUC.listAll().stream().map(webMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorretorResponse> buscar(@PathVariable Integer id) {
        return getUC.findById(new CorretorId(id))
                .map(webMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('CORRETOR')")
    @GetMapping("/me")
    public ResponseEntity<CorretorResponse> meuPerfil() {
        return getCurrentUC.findCurrentCorretor()
                .map(webMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping
    public ResponseEntity<CorretorResponse> criar(@Valid @RequestBody CorretorCreateRequest req) {
        var created = createUC.create(webMapper.toDomain(req));
        var resp = webMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/corretores/" + resp.idCorretor())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CorretorResponse> atualizar(@PathVariable Integer id, @Valid @RequestBody CorretorUpdateRequest req) {
        return updateUC.update(new CorretorId(id), webMapper.toDomain(id, req))
                .map(webMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        boolean ok = deleteUC.delete(new CorretorId(id));
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
