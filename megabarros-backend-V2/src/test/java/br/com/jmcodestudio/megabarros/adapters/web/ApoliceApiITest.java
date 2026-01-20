package br.com.jmcodestudio.megabarros.adapters.web;

import br.com.jmcodestudio.megabarros.adapters.web.support.JwtTestUtils;
import br.com.jmcodestudio.megabarros.application.port.out.TokenServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc // filtros ATIVOS
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class ApoliceApiITest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("megabarros_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "public");
        registry.add("spring.flyway.schemas", () -> "public");
        registry.add("spring.flyway.defaultSchema", () -> "public");

        // Propriedades JWT usadas pelo JwtTokenService
        registry.add("JWT_ISSUER", () -> "megabarros-v2");
        registry.add("JWT_AUDIENCE", () -> "megabarros-frontend");
        registry.add("JWT_SECRET", () -> "test-secret-32-bytes-minimum-1234567890");
        registry.add("JWT_ACCESS_EXP_SECONDS", () -> "3600");
        registry.add("JWT_REFRESH_EXP_SECONDS", () -> "1209600");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtTestUtils jwt;

    @Autowired
    TokenServicePort tokens;

    Long usuarioId;
    Integer corretorId;
    Integer clienteId;
    Integer corretorClienteId;
    Integer seguradoraId;
    Integer produtoId;

    @BeforeEach
    void setup() {
        jdbc.execute("SET search_path TO public");
        jdbc.update("""
            TRUNCATE TABLE
                contato,
                apolice_cobertura,
                parcela_apolice,
                apolice_status,
                apolice,
                corretor_cliente,
                corretor,
                cliente_endereco,
                beneficiario,
                cliente,
                produto,
                seguradora,
                refresh_token,
                usuario
            RESTART IDENTITY CASCADE
        """);

        usuarioId = jdbc.queryForObject("""
            INSERT INTO usuario (nome_usuario, email, senha_hash, perfil_usuario, ativo, must_change_password)
            VALUES ('Admin Teste', 'admin.teste@example.com', '$2a$12$abcdefghijklmnopqrstuv..../abcdefghijklmno123456', 'admin', true, false)
            RETURNING id_usuario
        """, Long.class);
        assertThat(usuarioId).isNotNull();

        corretorId = jdbc.queryForObject("""
            INSERT INTO corretor (id_usuario, nome_corretor, uf)
            VALUES (?, 'Corretor Testes', 'SP') RETURNING id_corretor
        """, Integer.class, usuarioId);
        assertThat(corretorId).isNotNull();

        clienteId = jdbc.queryForObject("""
            INSERT INTO cliente (nome_cliente, cpf_cnpj, email, telefone)
            VALUES ('Cliente Teste', '00000000001', 'cliente.teste@example.com', '(11) 90000-0000')
            RETURNING id_cliente
        """, Integer.class);
        assertThat(clienteId).isNotNull();

        corretorClienteId = jdbc.queryForObject("""
            INSERT INTO corretor_cliente (id_corretor, id_cliente)
            VALUES (?, ?) RETURNING id_corretor_cliente
        """, Integer.class, corretorId, clienteId);
        assertThat(corretorClienteId).isNotNull();

        seguradoraId = jdbc.queryForObject("""
            INSERT INTO seguradora (nome_seguradora) VALUES ('Seguradora Teste') RETURNING id_seguradora
        """, Integer.class);
        assertThat(seguradoraId).isNotNull();

        produtoId = jdbc.queryForObject("""
            INSERT INTO produto (nome_produto, tipo_produto, id_seguradora)
            VALUES ('Produto Teste', 'AUTO', ?) RETURNING id_produto
        """, Integer.class, seguradoraId);
        assertThat(produtoId).isNotNull();
    }


    private String criarApoliceJson(String numero,
                                    LocalDate emissao,
                                    LocalDate inicio,
                                    LocalDate fim,
                                    BigDecimal valor,
                                    BigDecimal comissao,
                                    String tipoContrato,
                                    Integer idCorretorCliente,
                                    Integer idProduto,
                                    Integer idSeguradora) throws Exception {
        Map<String, Object> body = Map.ofEntries(
                Map.entry("numeroApolice", numero),
                Map.entry("dataEmissao", emissao.toString()),
                Map.entry("vigenciaInicio", inicio.toString()),
                Map.entry("vigenciaFim", fim.toString()),
                Map.entry("valor", valor),
                Map.entry("comissaoPercentual", comissao),
                Map.entry("tipoContrato", tipoContrato),
                Map.entry("idCorretorCliente", idCorretorCliente),
                Map.entry("idProduto", idProduto),
                Map.entry("idSeguradora", idSeguradora)
        );
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldCreateListAndGetApolice() throws Exception {
        String body = criarApoliceJson(
                "APO-0001",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                new BigDecimal("1500.00"),
                new BigDecimal("10.00"),
                "ANUAL",
                corretorClienteId,
                produtoId,
                seguradoraId
        );

        var mvcRes = mockMvc.perform(post("/api/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idApolice").isNumber())
                .andExpect(jsonPath("$.numeroApolice").value("APO-0001"))
                .andExpect(jsonPath("$.statusAtual").value("ATIVA"))
                .andReturn();

        var created = objectMapper.readValue(mvcRes.getResponse().getContentAsString(), Map.class);
        Integer apoliceId = (Integer) created.get("idApolice");
        assertThat(apoliceId).isNotNull();

        mockMvc.perform(get("/api/apolices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idApolice").value(apoliceId))
                .andExpect(jsonPath("$[0].numeroApolice").value("APO-0001"));

        mockMvc.perform(get("/api/apolices/{id}", apoliceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idApolice").value(apoliceId))
                .andExpect(jsonPath("$.numeroApolice").value("APO-0001"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldUpdateApolice() throws Exception {
        // cria
        String body = criarApoliceJson(
                "APO-0002",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                new BigDecimal("1000.00"),
                new BigDecimal("8.50"),
                "ANUAL",
                corretorClienteId,
                produtoId,
                seguradoraId
        );
        Integer apoliceId = objectMapper.readValue(
                        mockMvc.perform(post("/api/apolices")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString(), Map.class)
                .get("idApolice") instanceof Integer i ? i : null;
        assertThat(apoliceId).isNotNull();

        // atualiza valor e comissão
        String updateJson = objectMapper.writeValueAsString(Map.of(
                "valor", new BigDecimal("1250.00"),
                "comissaoPercentual", new BigDecimal("9.00")
        ));
        mockMvc.perform(put("/api/apolices/{id}", apoliceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(1250.00))
                .andExpect(jsonPath("$.comissaoPercentual").value(9.00));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldCancelApolice() throws Exception {
        // cria
        String body = criarApoliceJson(
                "APO-0003",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                new BigDecimal("800.00"),
                new BigDecimal("12.00"),
                "ANUAL",
                corretorClienteId,
                produtoId,
                seguradoraId
        );
        Integer apoliceId = objectMapper.readValue(
                        mockMvc.perform(post("/api/apolices")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString(), Map.class)
                .get("idApolice") instanceof Integer i ? i : null;
        assertThat(apoliceId).isNotNull();

        // cancela
        mockMvc.perform(post("/api/apolices/{id}/cancel", apoliceId))
                .andExpect(status().isNoContent());

        // verifica status CANCELADA
        mockMvc.perform(get("/api/apolices/{id}", apoliceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAtual").value("CANCELADA"));
    }

    @Test
    @Order(4)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldAddAndPayParcela() throws Exception {
        // cria apólice
        String body = criarApoliceJson(
                "APO-0004",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                new BigDecimal("2000.00"),
                new BigDecimal("10.00"),
                "ANUAL",
                corretorClienteId,
                produtoId,
                seguradoraId
        );
        Integer apoliceId = objectMapper.readValue(
                        mockMvc.perform(post("/api/apolices")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString(), Map.class)
                .get("idApolice") instanceof Integer i ? i : null;
        assertThat(apoliceId).isNotNull();

        // adiciona parcela
        String parcelaJson = objectMapper.writeValueAsString(Map.of(
                "numeroParcela", 1,
                "dataVencimento", LocalDate.now().plusMonths(1).toString(),
                "valorParcela", new BigDecimal("1000.00")
        ));
        var mvcRes = mockMvc.perform(post("/api/apolices/{id}/parcelas", apoliceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parcelaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idParcela").isNumber())
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"))
                .andReturn();

        Integer parcelaId = (Integer) objectMapper.readValue(mvcRes.getResponse().getContentAsString(), Map.class).get("idParcela");
        assertThat(parcelaId).isNotNull();

        // marca como paga
        mockMvc.perform(post("/api/apolices/parcelas/{parcelaId}/pay", parcelaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idParcela").value(parcelaId))
                .andExpect(jsonPath("$.statusPagamento").value("PAGA"))
                .andExpect(jsonPath("$.dataPagamento").isNotEmpty());
    }

    // NEGATIVO: CORRETOR não pode criar apólice
    @Test
    @Order(5)
    @WithMockUser(username = "corretor.teste@example.com", roles = {"CORRETOR"})
    void shouldForbidCreateApoliceAsCorretor() throws Exception {
        String body = criarApoliceJson(
                "APO-0005",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                new BigDecimal("1200.00"),
                new BigDecimal("7.50"),
                "ANUAL",
                corretorClienteId,
                produtoId,
                seguradoraId
        );

        mockMvc.perform(post("/api/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldRejectInvalidVigencia() throws Exception {
        // criar apólice com vigência fim < início
        String body = objectMapper.writeValueAsString(Map.of(
                "numeroApolice", "APO-INV-001",
                "dataEmissao", LocalDate.now().toString(),
                "vigenciaInicio", LocalDate.now().plusDays(10).toString(),
                "vigenciaFim", LocalDate.now().plusDays(5).toString(),
                "valor", new BigDecimal("1000.00"),
                "comissaoPercentual", new BigDecimal("10.00"),
                "tipoContrato", "ANUAL",
                "idCorretorCliente", corretorClienteId,
                "idProduto", produtoId,
                "idSeguradora", seguradoraId
        ));

        mockMvc.perform(post("/api/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldRejectDuplicateNumeroApolice() throws Exception {
        // cria primeira apólice
        String body1 = criarApoliceJson(
                "APO-DUP-001", LocalDate.now(), LocalDate.now(), LocalDate.now().plusYears(1),
                new BigDecimal("1000.00"), new BigDecimal("10.00"), "ANUAL",
                corretorClienteId, produtoId, seguradoraId
        );
        mockMvc.perform(post("/api/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isCreated());

        // tenta segunda com mesmo número
        String body2 = criarApoliceJson(
                "APO-DUP-001", LocalDate.now(), LocalDate.now(), LocalDate.now().plusYears(1),
                new BigDecimal("1200.00"), new BigDecimal("9.00"), "ANUAL",
                corretorClienteId, produtoId, seguradoraId
        );
        mockMvc.perform(post("/api/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(8)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void cancelShouldClosePreviousStatusAndCreateCanceled() throws Exception {
        // cria apólice
        String body = criarApoliceJson(
                "APO-CAN-001", LocalDate.now(), LocalDate.now(), LocalDate.now().plusYears(1),
                new BigDecimal("800.00"), new BigDecimal("12.00"), "ANUAL",
                corretorClienteId, produtoId, seguradoraId
        );
        Integer apoliceId = objectMapper.readValue(
                        mockMvc.perform(post("/api/apolices")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString(), Map.class)
                .get("idApolice") instanceof Integer i ? i : null;
        assertThat(apoliceId).isNotNull();

        // cancela
        mockMvc.perform(post("/api/apolices/{id}/cancel", apoliceId))
                .andExpect(status().isNoContent());

        // verifica status atual CANCELADA
        mockMvc.perform(get("/api/apolices/{id}", apoliceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAtual").value("CANCELADA"));

        // verifica no banco: existe ATIVA com data_fim preenchida e CANCELADA com data_fim nula
        Integer ativaSemFim = jdbc.queryForObject("""
            SELECT COUNT(*) FROM apolice_status 
            WHERE id_apolice = ? AND status = 'ATIVA' AND data_fim IS NOT NULL
        """, Integer.class, apoliceId);
        Integer canceladaSemFim = jdbc.queryForObject("""
            SELECT COUNT(*) FROM apolice_status 
            WHERE id_apolice = ? AND status = 'CANCELADA' AND data_fim IS NULL
        """, Integer.class, apoliceId);

        assertThat(ativaSemFim).isEqualTo(1);
        assertThat(canceladaSemFim).isEqualTo(1);
    }

    @Test
    @Order(9)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldReturnBadRequestOnMissingRequiredFields() throws Exception {
        // Falta numeroApolice e datas inválidas -> 400
        String body = objectMapper.writeValueAsString(Map.of(
                "valor", new BigDecimal("1000.00"),
                "comissaoPercentual", new BigDecimal("10.00"),
                "tipoContrato", "ANUAL",
                "idCorretorCliente", corretorClienteId,
                "idProduto", produtoId,
                "idSeguradora", seguradoraId
        ));
        mockMvc.perform(post("/api/apolices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    void shouldListApolicesWithBearerToken() throws Exception {
        // Gera token ADMIN para o usuário criado no setup
        String token = tokens.generateAccessToken(usuarioId, "admin.teste@example.com", "ADMIN", Map.of(), Instant.now());

        mockMvc.perform(get("/api/apolices")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

}