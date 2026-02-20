package br.com.jmcodestudio.megabarros.adapters.web.controller.usuario;

import br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.usuario.UsuarioWebMapper;
import br.com.jmcodestudio.megabarros.adapters.web.dto.usuario.UsuarioCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.usuario.UsuarioResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.usuario.UsuarioUpdateRequest;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.CreateUsuarioUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.DeleteUsuarioUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.GetUsuarioUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.ListUsuariosUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.UpdateUsuarioUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    private final CreateUsuarioUseCase createUC;
    private final ListUsuariosUseCase listUC;
    private final GetUsuarioUseCase getUC;
    private final DeleteUsuarioUseCase deleteUC;
    private final UpdateUsuarioUseCase updateUC;
    private final UsuarioWebMapper mapper;

    public UsuarioController(CreateUsuarioUseCase createUC,
                             ListUsuariosUseCase listUC,
                             GetUsuarioUseCase getUC,
                             DeleteUsuarioUseCase deleteUC,
                             UpdateUsuarioUseCase updateUC,
                             UsuarioWebMapper mapper) {
        this.createUC = createUC;
        this.listUC = listUC;
        this.getUC = getUC;
        this.deleteUC = deleteUC;
        this.updateUC = updateUC;
        this.mapper = mapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioCreateRequest req) {
        var created = createUC.create(mapper.toCommand(req));
        var resp = mapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/usuarios/" + resp.idUsuario())).body(resp);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        var list = listUC.listAll().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id) {
        return getUC.getById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest req) {
        return updateUC.update(id, mapper.toCommand(req))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        boolean ok = deleteUC.deleteById(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}