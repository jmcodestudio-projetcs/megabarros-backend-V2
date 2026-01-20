package br.com.jmcodestudio.megabarros.adapters.web.controller.apolice;

import br.com.jmcodestudio.megabarros.adapters.web.dto.apolice.ApoliceCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.apolice.ApoliceResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.apolice.ApoliceUpdateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.apolice.ApoliceWebMapper;
import br.com.jmcodestudio.megabarros.adapters.web.dto.parcela.ParcelaRequest;
import br.com.jmcodestudio.megabarros.application.domain.apolice.Apolice;
import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;
import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;
import br.com.jmcodestudio.megabarros.application.port.in.apolice.CancelApoliceUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.apolice.CreateApoliceUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.apolice.ListApolicesUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.apolice.UpdateApoliceUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.parcela.ParcelaUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/apolices")
@CrossOrigin(origins = "http://localhost:5173")
public class ApoliceController {

    private final CreateApoliceUseCase createUC;
    private final UpdateApoliceUseCase updateUC;
    private final ListApolicesUseCase listUC;
    private final CancelApoliceUseCase cancelUC;
    private final ParcelaUseCase parcelaUC;
    private final ApoliceWebMapper webMapper;

    public ApoliceController(CreateApoliceUseCase createUC,
                             UpdateApoliceUseCase updateUC,
                             ListApolicesUseCase listUC,
                             CancelApoliceUseCase cancelUC,
                             ParcelaUseCase parcelaUC,
                             ApoliceWebMapper webMapper) {
        this.createUC = createUC;
        this.updateUC = updateUC;
        this.listUC = listUC;
        this.cancelUC = cancelUC;
        this.parcelaUC = parcelaUC;
        this.webMapper = webMapper;
    }

    // Leitura aberta a ADMIN, USUARIO e CORRETOR
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO','CORRETOR')")
    @GetMapping
    public ResponseEntity<List<ApoliceResponse>> listar(@RequestParam(required = false) Integer seguradoraId,
                                                        @RequestParam(required = false) Integer produtoId,
                                                        @RequestParam(required = false) Integer corretorClienteId) {
        List<Apolice> list;
        if (seguradoraId != null) list = listUC.listBySeguradora(seguradoraId);
        else if (produtoId != null) list = listUC.listByProduto(produtoId);
        else if (corretorClienteId != null) list = listUC.listByCorretorCliente(corretorClienteId);
        else list = listUC.listAll();
        var resp = list.stream().map(webMapper::toResponse).toList();
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO','CORRETOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApoliceResponse> buscar(@PathVariable Integer id) {
        return listUC.getById(new ApoliceId(id))
                .map(webMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Escrita restrita
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping
    public ResponseEntity<ApoliceResponse> criar(@Valid @RequestBody ApoliceCreateRequest req) {
        Apolice domain = webMapper.toDomain(req);
        domain = new Apolice(
                null,
                domain.numeroApolice(),
                domain.dataEmissao(),
                domain.vigenciaInicio(),
                domain.vigenciaFim(),
                domain.valor(),
                domain.comissaoPercentual(),
                domain.tipoContrato(),
                req.idCorretorCliente(),
                req.idProduto(),
                req.idSeguradora(),
                null,
                domain.parcelas(),
                List.of(), // coberturas: não usadas nesta fase
                List.of()  // beneficiários: não usados nesta fase
        );
        Apolice created = createUC.create(domain);
        var resp = webMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/apolices/" + resp.idApolice())).body(resp);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<ApoliceResponse> atualizar(@PathVariable Integer id, @Valid @RequestBody ApoliceUpdateRequest req) {
        return updateUC.update(new ApoliceId(id), webMapper.toDomain(id, req))
                .map(webMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelar(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        cancelUC.cancel(new ApoliceId(id), reason);
        return ResponseEntity.noContent().build();
    }

    // Parcela
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping("/{id}/parcelas")
    public ResponseEntity<ApoliceResponse.ParcelaResponse> adicionarParcela(@PathVariable Integer id, @Valid @RequestBody ParcelaRequest req) {
        Parcela p = webMapper.toParcela(id, req);
        Parcela saved = parcelaUC.addParcela(p);
        return ResponseEntity.created(URI.create("/api/apolices/parcelas/" + saved.id()))
                .body(new ApoliceResponse.ParcelaResponse(saved.id(), saved.numeroParcela(), saved.dataVencimento(), saved.valorParcela(), saved.statusPagamento(), saved.dataPagamento()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @PostMapping("/parcelas/{parcelaId}/pay")
    public ResponseEntity<ApoliceResponse.ParcelaResponse> pagarParcela(@PathVariable Integer parcelaId) {
        return parcelaUC.markPaid(parcelaId)
                .map(p -> new ApoliceResponse.ParcelaResponse(p.id(), p.numeroParcela(), p.dataVencimento(), p.valorParcela(), p.statusPagamento(), p.dataPagamento()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Removidos nesta fase:
    // - adicionarBeneficiario
    // - adicionarCobertura
}