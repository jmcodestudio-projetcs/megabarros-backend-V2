package br.com.jmcodestudio.megabarros.adapters.web.controller.corretorCliente;

import br.com.jmcodestudio.megabarros.adapters.web.dto.corretorCliente.CorretorClienteCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.corretorCliente.CorretorClienteResponse;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorClienteCommandPort;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorClienteQueryPort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class CorretorClienteController {

    private final CorretorClienteQueryPort queryPort;
    private final CorretorClienteCommandPort commandPort;

    public CorretorClienteController(CorretorClienteQueryPort queryPort, CorretorClienteCommandPort commandPort) {
        this.queryPort = queryPort;
        this.commandPort = commandPort;
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO','CORRETOR')")
    @GetMapping("/clientes/{clienteId}/corretores")
    public ResponseEntity<List<CorretorClienteResponse>> listCorretoresByCliente(@PathVariable Integer clienteId) {
        var list = queryPort.listByClienteId(clienteId).stream()
                .map(l -> new CorretorClienteResponse(
                        l.idCorretorCliente(),
                        l.idCorretor(),
                        l.idCliente(),
                        l.nomeCorretor(),
                        l.uf(),
                        l.email(),
                        l.dataInicio()
                ))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO','CORRETOR')")
    @GetMapping("/corretor-clientes/resolve")
    public ResponseEntity<Integer> resolveId(@RequestParam Integer clienteId, @RequestParam Integer corretorId) {
        return queryPort.findIdByCorretorIdAndClienteId(corretorId, clienteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Novo: cria vínculo corretor-cliente (idempotente)
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping("/corretor-clientes")
    public ResponseEntity<Map<String, Integer>> createLink(@Valid @RequestBody CorretorClienteCreateRequest req) {
        // valida existência para 404 amigável
        if (!commandPort.existsCorretor(req.corretorId()) || !commandPort.existsCliente(req.clienteId())) {
            return ResponseEntity.notFound().build();
        }
        Integer id = commandPort.createLink(req.corretorId(), req.clienteId());
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(URI.create("/api/corretor-clientes/" + id))
                .body(Map.of("idCorretorCliente", id));
    }

    // Novo: desvincula por id do vínculo
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @DeleteMapping("/corretor-clientes/{id}")
    public ResponseEntity<Void> deleteLink(@PathVariable Integer id) {
        if (!commandPort.existsLink(id)) {
            return ResponseEntity.notFound().build();
        }
        commandPort.deleteLink(id);
        return ResponseEntity.noContent().build();
    }
}