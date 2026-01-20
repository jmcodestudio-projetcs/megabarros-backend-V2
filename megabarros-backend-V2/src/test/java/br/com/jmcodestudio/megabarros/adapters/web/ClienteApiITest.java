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
class ClienteApiITest {

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

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper objectMapper;
    @Autowired
    TokenServicePort tokens;

    Long usuarioIdAdmin;
    Long usuarioIdCorretor;
    Integer corretorId;
    Integer clienteIdVinculado;

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

        usuarioIdAdmin = jdbc.queryForObject("""
            INSERT INTO usuario (nome_usuario, email, senha_hash, perfil_usuario, ativo, must_change_password)
            VALUES ('Admin', 'admin@example.com', 'x', 'admin', true, false)
            RETURNING id_usuario
        """, Long.class);
        assertThat(usuarioIdAdmin).isNotNull();

        usuarioIdCorretor = jdbc.queryForObject("""
            INSERT INTO usuario (nome_usuario, email, senha_hash, perfil_usuario, ativo, must_change_password)
            VALUES ('Corretor', 'corretor@example.com', 'x', 'corretor', true, false)
            RETURNING id_usuario
        """, Long.class);
        assertThat(usuarioIdCorretor).isNotNull();

        corretorId = jdbc.queryForObject("""
            INSERT INTO corretor (id_usuario, nome_corretor, uf) VALUES (?, 'Corretor Teste', 'SP') RETURNING id_corretor
        """, Integer.class, usuarioIdCorretor);

        clienteIdVinculado = jdbc.queryForObject("""
            INSERT INTO cliente (nome_cliente, cpf_cnpj, data_nascimento, email, telefone, ativo)
            VALUES ('Cliente Vinculado', '00000000001', '1990-01-01', 'vinculado@example.com', '(11) 90000-0000', true)
            RETURNING id_cliente
        """, Integer.class);
        jdbc.update("INSERT INTO corretor_cliente (id_corretor, id_cliente) VALUES (?, ?)", corretorId, clienteIdVinculado);
    }

    private String jsonCliente(String nome, String cpf, String nasc, String email, String telefone) throws Exception {
        Map<String, Object> body = Map.of(
                "nome", nome,
                "cpfCnpj", cpf,
                "dataNascimento", nasc,
                "email", email,
                "telefone", telefone
        );
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldCreateListAndGetClienteAsAdmin() throws Exception {
        String body = jsonCliente("Cliente A", "12345678900", LocalDate.of(1995, 5, 10).toString(), "a@example.com", "(11) 90000-0001");

        var mvcRes = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCliente").isNumber())
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn();

        Integer id = (Integer) objectMapper.readValue(mvcRes.getResponse().getContentAsString(), Map.class).get("idCliente");

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCliente").isNumber());

        mockMvc.perform(get("/api/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(id))
                .andExpect(jsonPath("$.nome").value("Cliente A"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "corretor@example.com", roles = {"CORRETOR"})
    void corretorShouldUpdateOnlyEmailAndTelefoneOfLinkedCliente() throws Exception {
        // tentar alterar nome deve ser proibido
        String tentativa = objectMapper.writeValueAsString(Map.of("nome", "Novo Nome"));
        mockMvc.perform(put("/api/clientes/{id}", clienteIdVinculado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tentativa))
                .andExpect(status().isForbidden());

        // alterar email e telefone deve ser permitido
        String contato = objectMapper.writeValueAsString(Map.of(
                "email", "newmail@example.com",
                "telefone", "(11) 98888-0000"
        ));
        mockMvc.perform(put("/api/clientes/{id}", clienteIdVinculado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contato))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newmail@example.com"))
                .andExpect(jsonPath("$.telefone").value("(11) 98888-0000"));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "corretor@example.com", roles = {"CORRETOR"})
    void corretorShouldForbidUpdateUnlinkedClienteEvenForContato() throws Exception {
        Integer outroCliente = jdbc.queryForObject("""
            INSERT INTO cliente (nome_cliente, cpf_cnpj, data_nascimento, email, telefone, ativo)
            VALUES ('Outro', '00000000002', '1992-02-02', 'outro@example.com', '(11) 90000-0002', true)
            RETURNING id_cliente
        """, Integer.class);

        String contato = objectMapper.writeValueAsString(Map.of(
                "email", "proibido@example.com",
                "telefone", "(11) 97777-0000"
        ));
        mockMvc.perform(put("/api/clientes/{id}", outroCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contato))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void adminShouldBlockDeactivateClienteWithMultipleActiveApolicesThenAllowAfterCancel() throws Exception {
        // cria seguradora e produto
        Integer segId = jdbc.queryForObject("INSERT INTO seguradora (nome_seguradora) VALUES ('Seg Test') RETURNING id_seguradora", Integer.class);
        Integer prodId = jdbc.queryForObject("INSERT INTO produto (nome_produto, tipo_produto, id_seguradora) VALUES ('Prod Test', 'AUTO', ?) RETURNING id_produto", Integer.class, segId);

        Integer ccId = jdbc.queryForObject("SELECT id_corretor_cliente FROM corretor_cliente WHERE id_cliente = ?", Integer.class, clienteIdVinculado);
        assertThat(ccId).isNotNull();

        // duas apólices ATIVAS
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('APO-ATIVA-001', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 1000.00, 10.00, 'ANUAL', ?, ?, ?)
        """, ccId, prodId, segId);
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('APO-ATIVA-002', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 1200.00, 9.50, 'ANUAL', ?, ?, ?)
        """, ccId, prodId, segId);

        Integer apoliceId1 = jdbc.queryForObject("SELECT id_apolice FROM apolice WHERE numero_apolice = 'APO-ATIVA-001'", Integer.class);
        Integer apoliceId2 = jdbc.queryForObject("SELECT id_apolice FROM apolice WHERE numero_apolice = 'APO-ATIVA-002'", Integer.class);
        jdbc.update("INSERT INTO apolice_status (id_apolice, status, data_inicio) VALUES (?, 'ATIVA', now())", apoliceId1);
        jdbc.update("INSERT INTO apolice_status (id_apolice, status, data_inicio) VALUES (?, 'ATIVA', now())", apoliceId2);

        // tenta desativar -> 409
        mockMvc.perform(post("/api/clientes/{id}/desativar", clienteIdVinculado))
                .andExpect(status().isConflict());

        // cancela ambas apólices
        mockMvc.perform(post("/api/apolices/{id}/cancel", apoliceId1))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/apolices/{id}/cancel", apoliceId2))
                .andExpect(status().isNoContent());

        // agora deve permitir desativar -> 204
        mockMvc.perform(post("/api/clientes/{id}/desativar", clienteIdVinculado))
                .andExpect(status().isNoContent());

        // verifica ativo=false
        mockMvc.perform(get("/api/clientes/{id}", clienteIdVinculado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    @Order(5)
    @WithMockUser(username = "corretor@example.com", roles = {"CORRETOR"})
    void corretorCannotCreateOrDeactivate() throws Exception {
        String body = jsonCliente("Cliente B", "99999999999", LocalDate.of(1990, 1, 1).toString(), "b@example.com", "(11) 95555-0000");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/clientes/{id}/desativar", clienteIdVinculado))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldReturnBadRequestOnInvalidClientePayload() throws Exception {
        // Falta campos obrigatórios -> 400
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "", // NotBlank
                "cpfCnpj", "", // NotBlank
                "email", "invalido", // Email
                "telefone", "" // NotBlank
                // dataNascimento ausente
        ));
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deactivateShouldFailWhenAnyActiveApoliceExistsAndSucceedAfterCancelAll() throws Exception {
        // cria cliente novo
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Cliente C",
                "cpfCnpj", "11122233344",
                "dataNascimento", LocalDate.of(1990, 1, 1).toString(),
                "email", "cliente.c@example.com",
                "telefone", "(11) 90000-0003"
        ));
        var mvcRes = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer idCliente = (Integer) objectMapper.readValue(mvcRes.getResponse().getContentAsString(), Map.class).get("idCliente");

        // vincula ao corretor existente
        jdbc.update("INSERT INTO corretor_cliente (id_corretor, id_cliente) VALUES (?, ?)", corretorId, idCliente);

        // seguradora/produto
        Integer segId = jdbc.queryForObject("INSERT INTO seguradora (nome_seguradora) VALUES ('Seg Test 2') RETURNING id_seguradora", Integer.class);
        Integer prodId = jdbc.queryForObject("INSERT INTO produto (nome_produto, tipo_produto, id_seguradora) VALUES ('Prod Test 2', 'AUTO', ?) RETURNING id_produto", Integer.class, segId);
        Integer ccId = jdbc.queryForObject("SELECT id_corretor_cliente FROM corretor_cliente WHERE id_cliente = ?", Integer.class, idCliente);

        // duas apólices ATIVAS
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('APO-C-ID-001', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 900.00, 10.00, 'ANUAL', ?, ?, ?)
        """, ccId, prodId, segId);
        jdbc.update("""
            INSERT INTO apolice (numero_apolice, data_emissao, vigencia_inicio, vigencia_fim, valor, comissao_percentual, tipo_contrato,
                                 id_corretor_cliente, id_produto, id_seguradora)
            VALUES ('APO-C-ID-002', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 950.00, 9.50, 'ANUAL', ?, ?, ?)
        """, ccId, prodId, segId);

        Integer ap1 = jdbc.queryForObject("SELECT id_apolice FROM apolice WHERE numero_apolice = 'APO-C-ID-001'", Integer.class);
        Integer ap2 = jdbc.queryForObject("SELECT id_apolice FROM apolice WHERE numero_apolice = 'APO-C-ID-002'", Integer.class);
        jdbc.update("INSERT INTO apolice_status (id_apolice, status, data_inicio) VALUES (?, 'ATIVA', now())", ap1);
        jdbc.update("INSERT INTO apolice_status (id_apolice, status, data_inicio) VALUES (?, 'ATIVA', now())", ap2);

        // desativação deve falhar -> 409
        mockMvc.perform(post("/api/clientes/{id}/desativar", idCliente))
                .andExpect(status().isConflict());

        // cancela as apólices
        mockMvc.perform(post("/api/apolices/{id}/cancel", ap1))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/apolices/{id}/cancel", ap2))
                .andExpect(status().isNoContent());

        // desativação deve funcionar -> 204
        mockMvc.perform(post("/api/clientes/{id}/desativar", idCliente))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(8)
    void shouldListClientesWithBearerTokenAsAdmin() throws Exception {
        String token = tokens.generateAccessToken(usuarioIdAdmin, "admin@example.com", "ADMIN", Map.of(), Instant.now());
        mockMvc.perform(get("/api/clientes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    void corretorShouldUpdateContatoWithBearerToken() throws Exception {
        String token = tokens.generateAccessToken(usuarioIdCorretor, "corretor@example.com", "CORRETOR", Map.of(), Instant.now());

        String contato = objectMapper.writeValueAsString(Map.of(
                "email", "viaBearer@example.com",
                "telefone", "(11) 96666-0000"
        ));
        mockMvc.perform(put("/api/clientes/{id}", clienteIdVinculado)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contato))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("viaBearer@example.com"))
                .andExpect(jsonPath("$.telefone").value("(11) 96666-0000"));
    }
}
