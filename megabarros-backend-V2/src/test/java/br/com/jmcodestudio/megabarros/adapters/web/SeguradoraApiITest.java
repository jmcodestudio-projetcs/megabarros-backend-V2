package br.com.jmcodestudio.megabarros.adapters.web;

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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc // filtros ATIVOS
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class SeguradoraApiITest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("megabarros_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "public");
        registry.add("spring.flyway.schemas", () -> "public");
        registry.add("spring.flyway.defaultSchema", () -> "public");

        // JWT
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
    TokenServicePort tokens;

    Long adminId;

    Long usuarioId;
    Integer corretorId;
    Integer clienteId;
    Integer corretorClienteId;

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

        adminId = jdbc.queryForObject("""
            INSERT INTO usuario (nome_usuario, email, senha_hash, perfil_usuario, ativo, must_change_password)
            VALUES ('Admin', 'admin@example.com', 'x', 'admin', true, false)
            RETURNING id_usuario
        """, Long.class);
        assertThat(adminId).isNotNull();
    }

    private String criarSeguradoraJson(String nome, List<Map<String, Object>> produtos) throws Exception {
        Map<String, Object> body = Map.ofEntries(
                Map.entry("nomeSeguradora", nome),
                Map.entry("produtos", produtos)
        );
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldCreateSeguradoraWithProductsAndCountsZero() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Seg A",
                "produtos", List.of(
                        Map.of("nomeProduto", "Auto Básico", "tipoProduto", "AUTO"),
                        Map.of("nomeProduto", "Vida Essencial", "tipoProduto", "VIDA")
                )
        ));

        var mvcRes = mockMvc.perform(post("/api/seguradoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSeguradora").isNumber())
                .andExpect(jsonPath("$.apoliceCount").value(0))
                .andExpect(jsonPath("$.produtos").isArray())
                .andExpect(jsonPath("$.produtos.length()").value(2))
                .andExpect(jsonPath("$.produtos[0].apoliceCount").value(0))
                .andExpect(jsonPath("$.produtos[1].apoliceCount").value(0))
                .andReturn();

        Integer segId = (Integer) objectMapper.readValue(mvcRes.getResponse().getContentAsString(), Map.class).get("idSeguradora");

        mockMvc.perform(get("/api/seguradoras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idSeguradora").value(segId))
                .andExpect(jsonPath("$[0].apoliceCount").value(0))
                .andExpect(jsonPath("$[0].produtos").isArray());
    }

    @Test
    @Order(2)
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldReturn409WhenDeletingSeguradoraWithApolices() throws Exception {
        // cria seguradora e produto
        Integer segId = jdbc.queryForObject("INSERT INTO seguradora (nome_seguradora) VALUES ('Seg B') RETURNING id_seguradora", Integer.class);
        Integer prodId = jdbc.queryForObject("INSERT INTO produto (nome_produto, tipo_produto, id_seguradora) VALUES ('Auto Pro', 'AUTO', ?) RETURNING id_produto", Integer.class, segId);

        // cria cliente/corretor e apólice vinculada
        Long corretorUser = jdbc.queryForObject("INSERT INTO usuario (nome_usuario, email, senha_hash, perfil_usuario, ativo, must_change_password) VALUES ('Corretor', 'cor@example.com', 'x', 'corretor', true, false) RETURNING id_usuario", Long.class);
        Integer corretorId = jdbc.queryForObject("INSERT INTO corretor (id_usuario, nome_corretor, uf) VALUES (?, 'Cor B', 'SP') RETURNING id_corretor", Integer.class, corretorUser);
        Integer cliId = jdbc.queryForObject("INSERT INTO cliente (nome_cliente, cpf_cnpj, email, telefone, ativo) VALUES ('Cliente', '12345678901', 'c@c.com', '(11) 9', true) RETURNING id_cliente", Integer.class);
        Integer ccId = jdbc.queryForObject("INSERT INTO corretor_cliente (id_corretor, id_cliente) VALUES (?, ?) RETURNING id_corretor_cliente", Integer.class, corretorId, cliId);
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('APO-SEG-DEL-001', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 1000.00, 10.0, 'ANUAL', ?, ?, ?)
        """, ccId, prodId, segId);
        Integer apId = jdbc.queryForObject("SELECT id_apolice FROM apolice WHERE numero_apolice = 'APO-SEG-DEL-001'", Integer.class);
        jdbc.update("INSERT INTO apolice_status (id_apolice, status, data_inicio) VALUES (?, 'ATIVA', now())", apId);

        // tenta deletar seguradora -> 409
        mockMvc.perform(delete("/api/seguradoras/{id}", segId))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldReturn409WhenDeletingProdutoWithApolices() throws Exception {
        Integer segId = jdbc.queryForObject("INSERT INTO seguradora (nome_seguradora) VALUES ('Seg C') RETURNING id_seguradora", Integer.class);
        Integer prodId = jdbc.queryForObject("INSERT INTO produto (nome_produto, tipo_produto, id_seguradora) VALUES ('Vida Total', 'VIDA', ?) RETURNING id_produto", Integer.class, segId);

        // cria cliente/corretor e apólice vinculada
        Long corretorUser = jdbc.queryForObject("INSERT INTO usuario (nome_usuario, email, senha_hash, perfil_usuario, ativo, must_change_password) VALUES ('Corretor', 'cor2@example.com', 'x', 'corretor', true, false) RETURNING id_usuario", Long.class);
        Integer corretorId = jdbc.queryForObject("INSERT INTO corretor (id_usuario, nome_corretor, uf) VALUES (?, 'Cor C', 'RJ') RETURNING id_corretor", Integer.class, corretorUser);
        Integer cliId = jdbc.queryForObject("INSERT INTO cliente (nome_cliente, cpf_cnpj, email, telefone, ativo) VALUES ('Cliente', '99999999999', 'c2@c.com', '(11) 9', true) RETURNING id_cliente", Integer.class);
        Integer ccId = jdbc.queryForObject("INSERT INTO corretor_cliente (id_corretor, id_cliente) VALUES (?, ?) RETURNING id_corretor_cliente", Integer.class, corretorId, cliId);
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('APO-PRD-DEL-001', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 1500.00, 8.0, 'ANUAL', ?, ?, ?)
        """, ccId, prodId, segId);
        Integer apId = jdbc.queryForObject("SELECT id_apolice FROM apolice WHERE numero_apolice = 'APO-PRD-DEL-001'", Integer.class);
        jdbc.update("INSERT INTO apolice_status (id_apolice, status, data_inicio) VALUES (?, 'ATIVA', now())", apId);

        // tenta deletar produto -> 409
        mockMvc.perform(delete("/api/seguradoras/produtos/{id}", prodId))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    void shouldCreateSeguradoraWithBearerTokenAndCountsZero() throws Exception {
        String token = tokens.generateAccessToken(adminId, "admin@example.com", "ADMIN", Map.of(), Instant.now());
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Seg D",
                "produtos", List.of(Map.of("nomeProduto", "Residencial", "tipoProduto", "RESIDENCIAL"))
        ));

        mockMvc.perform(post("/api/seguradoras")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apoliceCount").value(0))
                .andExpect(jsonPath("$.produtos[0].apoliceCount").value(0));
    }
}