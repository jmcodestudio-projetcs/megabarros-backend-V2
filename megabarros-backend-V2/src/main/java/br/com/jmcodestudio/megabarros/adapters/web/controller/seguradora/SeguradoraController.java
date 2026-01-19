package br.com.jmcodestudio.megabarros.adapters.web.controller.seguradora;

import br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.seguradora.SeguradoraWebMapper;
import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraUpdateRequest;
import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import br.com.jmcodestudio.megabarros.application.port.in.produto.CreateProdutoUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.produto.DeleteProdutoUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.seguradora.CreateSeguradoraUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.seguradora.DeleteSeguradoraUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.seguradora.ListSeguradorasUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.seguradora.UpdateSeguradoraUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceQueryPort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/seguradoras")
@CrossOrigin(origins = "http://localhost:5173")
public class SeguradoraController {

    private final CreateSeguradoraUseCase createUC;
    private final UpdateSeguradoraUseCase updateUC;
    private final DeleteSeguradoraUseCase deleteUC;
    private final ListSeguradorasUseCase listUC;
    private final CreateProdutoUseCase createProdutoUC;
    private final DeleteProdutoUseCase deleteProdutoUC;
    private final ApoliceQueryPort apoliceQuery;
    private final SeguradoraWebMapper webMapper;

    public SeguradoraController(CreateSeguradoraUseCase createUC,
                                UpdateSeguradoraUseCase updateUC,
                                DeleteSeguradoraUseCase deleteUC,
                                ListSeguradorasUseCase listUC,
                                CreateProdutoUseCase createProdutoUC,
                                DeleteProdutoUseCase deleteProdutoUC,
                                ApoliceQueryPort apoliceQuery,
                                SeguradoraWebMapper webMapper) {
        this.createUC = createUC;
        this.updateUC = updateUC;
        this.deleteUC = deleteUC;
        this.listUC = listUC;
        this.createProdutoUC = createProdutoUC;
        this.deleteProdutoUC = deleteProdutoUC;
        this.apoliceQuery = apoliceQuery;
        this.webMapper = webMapper;
    }

    @GetMapping
    public ResponseEntity<List<SeguradoraResponse>> listar() {
        var list = listUC.listAll().stream().map(this::toResponseWithCounts).toList();
        return ResponseEntity.ok(list);
    }

    // Somente ADMIN e USUARIO podem criar/alterar/excluir seguradora e produtos
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping
    public ResponseEntity<SeguradoraResponse> criar(@Valid @RequestBody SeguradoraCreateRequest req) {
        var domain = webMapper.toDomain(req);
        var created = createUC.create(domain);
        var resp = toResponseWithCounts(created);
        return ResponseEntity.created(URI.create("/api/seguradoras/" + resp.idSeguradora())).body(resp);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<SeguradoraResponse> atualizar(@PathVariable Integer id, @Valid @RequestBody SeguradoraUpdateRequest req) {
        return updateUC.update(new SeguradoraId(id), webMapper.toDomain(id, req))
                .map(this::toResponseWithCounts)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        deleteUC.delete(new SeguradoraId(id));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping("/{id}/produtos")
    public ResponseEntity<ProdutoResponse> criarProduto(@PathVariable Integer id, @Valid @RequestBody ProdutoRequest req) {
        Produto domain = webMapper.toDomain(req);
        domain = new Produto(null, new SeguradoraId(id), domain.nome(), domain.tipo());
        var created = createProdutoUC.create(domain);
        var resp = toProdutoResponseWithCount(created);
        return ResponseEntity.created(URI.create("/api/produtos/" + resp.idProduto())).body(resp);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Void> excluirProduto(@PathVariable Integer id) {
        deleteProdutoUC.delete(new ProdutoId(id));
        return ResponseEntity.noContent().build();
    }

    private SeguradoraResponse toResponseWithCounts(Seguradora s) {
        long segCount = apoliceQuery.countBySeguradoraId(s.id());
        List<ProdutoResponse> produtos = (s.produtos() == null ? List.<ProdutoResponse>of()
                : s.produtos().stream().map(this::toProdutoResponseWithCount).toList());
        return new SeguradoraResponse(s.id().value(), s.nome(), segCount, produtos);
    }

    private ProdutoResponse toProdutoResponseWithCount(Produto p) {
        long prodCount = (p.id() == null) ? 0 : apoliceQuery.countByProdutoId(p.id());
        return new ProdutoResponse(p.id() != null ? p.id().value() : null, p.nome(), p.tipo(), prodCount);
    }
}