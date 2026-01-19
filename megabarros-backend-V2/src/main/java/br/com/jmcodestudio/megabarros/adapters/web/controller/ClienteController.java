package br.com.jmcodestudio.megabarros.adapters.web.controller;

import br.com.jmcodestudio.megabarros.adapters.web.dto.cliente.ClienteCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.cliente.ClienteResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.cliente.ClienteUpdateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.cliente.ClienteWebMapper;
import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.CreateClienteUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.DeactivateClienteUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.ListClientesUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.UpdateClienteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:5173")
public class ClienteController {

    private final CreateClienteUseCase createUC;
    private final UpdateClienteUseCase updateUC;
    private final DeactivateClienteUseCase deactivateUC;
    private final ListClientesUseCase listUC;
    private final ClienteWebMapper webMapper;

    public ClienteController(CreateClienteUseCase createUC,
                             UpdateClienteUseCase updateUC,
                             DeactivateClienteUseCase deactivateUC,
                             ListClientesUseCase listUC,
                             ClienteWebMapper webMapper) {
        this.createUC = createUC;
        this.updateUC = updateUC;
        this.deactivateUC = deactivateUC;
        this.listUC = listUC;
        this.webMapper = webMapper;
    }

    // Listagem: ADMIN/USUARIO veem todos; CORRETOR vê os seus
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO','CORRETOR')")
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        var list = listUC.listAll().stream().map(webMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO','CORRETOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscar(@PathVariable Integer id) {
        return listUC.getById(id)
                .map(webMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Criar: somente ADMIN/USUARIO
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteCreateRequest req) {
        var created = createUC.create(webMapper.toDomain(req));
        var resp = webMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/clientes/" + resp.idCliente())).body(resp);
    }

    // Atualizar: ADMIN/USUARIO todos os campos; CORRETOR somente email/telefone
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO','CORRETOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Integer id, @RequestBody ClienteUpdateRequest req) {
        return updateUC.update(new ClienteId(id), webMapper.toDomain(id, req))
                .map(webMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Desativar: somente ADMIN/USUARIO
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Integer id) {
        deactivateUC.deactivate(new ClienteId(id));
        return ResponseEntity.noContent().build();
    }
}
