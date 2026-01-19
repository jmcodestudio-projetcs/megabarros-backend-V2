package br.com.jmcodestudio.megabarros.adapters.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class SeguradoraApiIT {

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

        // Propriedades mínimas de JWT caso algum bean as leia
        registry.add("security.jwt.secret", () -> "test-secret-32-bytes-minimum-1234567890");
        registry.add("security.jwt.issuer", () -> "megabarros-v2");
        registry.add("security.jwt.audience", () -> "megabarros-frontend");
        registry.add("security.jwt.access-token.ttl-minutes", () -> "60");
        registry.add("security.jwt.refresh-token.ttl-days", () -> "14");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ObjectMapper objectMapper;

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

        // cliente NÃO possui coluna 'uf' no baseline; inclua email/telefone
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
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldCreateSeguradoraWithProductsAndCountsZero() throws Exception {
        String body = criarSeguradoraJson("Seguradora A", List.of(
                Map.of("nomeProduto", "Produto 1", "tipoProduto", "TIPO_A"),
                Map.of("nomeProduto", "Produto 2", "tipoProduto", "TIPO_B")
        ));

        var mvcRes = mockMvc.perform(post("/api/seguradoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSeguradora").isNumber())
                .andExpect(jsonPath("$.nomeSeguradora").value("Seguradora A"))
                .andExpect(jsonPath("$.apoliceCount").value(0))
                .andExpect(jsonPath("$.produtos[0].apoliceCount").value(0))
                .andExpect(jsonPath("$.produtos[1].apoliceCount").value(0))
                .andReturn();

        var json = mvcRes.getResponse().getContentAsString();
        var map = objectMapper.readValue(json, Map.class);
        Integer segId = (Integer) map.get("idSeguradora");
        List<Map<String, Object>> prods = (List<Map<String, Object>>) map.get("produtos");
        Integer prod1Id = (Integer) prods.get(0).get("idProduto");
        Integer prod2Id = (Integer) prods.get(1).get("idProduto");

        // Insere apólices: 2 para produto 1 e 1 para produto 2
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('NUM-0001', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 1000.00, 10.00, 'ANUAL', ?, ?, ?)
        """, corretorClienteId, prod1Id, segId);
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('NUM-0002', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 1500.00, 8.50, 'ANUAL', ?, ?, ?)
        """, corretorClienteId, prod1Id, segId);
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('NUM-0003', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 800.00, 12.00, 'ANUAL', ?, ?, ?)
        """, corretorClienteId, prod2Id, segId);

        mockMvc.perform(get("/api/seguradoras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idSeguradora").value(segId))
                .andExpect(jsonPath("$[0].apoliceCount").value(3))
                .andExpect(jsonPath("$[0].produtos[0].apoliceCount").value(2))
                .andExpect(jsonPath("$[0].produtos[1].apoliceCount").value(1));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldBlockDeleteSeguradoraWhenHasApolices() throws Exception {
        String body = criarSeguradoraJson("Seguradora B", List.of());
        var mvcRes = mockMvc.perform(post("/api/seguradoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        var map = objectMapper.readValue(mvcRes.getResponse().getContentAsString(), Map.class);
        Integer segId = (Integer) map.get("idSeguradora");

        Integer prodId = jdbc.queryForObject("""
            INSERT INTO produto (nome_produto, tipo_produto, id_seguradora)
            VALUES ('Produto B1', 'TIPO_B', ?)
            RETURNING id_produto
        """, Integer.class, segId);

        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('NUM-0100', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 500.00, 5.00, 'ANUAL', ?, ?, ?)
        """, corretorClienteId, prodId, segId);

        mockMvc.perform(delete("/api/seguradoras/{id}", segId))
                .andExpect(status().isConflict());
    }

    // NEGATIVO: CORRETOR não pode criar seguradora
    @Test
    @Order(3)
    @WithMockUser(username = "corretor.teste@example.com", roles = {"CORRETOR"})
    void shouldForbidCreateSeguradoraAsCorretor() throws Exception {
        String body = criarSeguradoraJson("Seguradora C", List.of(
                Map.of("nomeProduto", "Produto C1", "tipoProduto", "AUTO")
        ));

        mockMvc.perform(post("/api/seguradoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // NEGATIVO: CORRETOR não pode criar produto
    @Test
    @Order(4)
    @WithMockUser(username = "corretor.teste@example.com", roles = {"CORRETOR"})
    void shouldForbidCreateProdutoAsCorretor() throws Exception {
        // cria seguradora diretamente no banco para ter um id
        Integer segId = jdbc.queryForObject("""
            INSERT INTO seguradora (nome_seguradora)
            VALUES ('Seguradora D')
            RETURNING id_seguradora
        """, Integer.class);
        assertThat(segId).isNotNull();

        String body = objectMapper.writeValueAsString(Map.of(
                "nomeProduto", "Produto D1",
                "tipoProduto", "VIDA"
        ));

        mockMvc.perform(post("/api/seguradoras/{id}/produtos", segId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}