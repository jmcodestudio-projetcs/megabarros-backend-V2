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

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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
        registry.add("security.jwt.secret", () -> "test-secret-32-bytes-minimum-1234567890");
        registry.add("security.jwt.issuer", () -> "megabarros-v2");
        registry.add("security.jwt.audience", () -> "megabarros-frontend");
        registry.add("security.jwt.access-token.ttl-minutes", () -> "60");
        registry.add("security.jwt.refresh-token.ttl-days", () -> "14");
    }

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper objectMapper;

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
    void corretorShouldNotSeeUnlinkedCliente() throws Exception {
        Integer outroCliente = jdbc.queryForObject("""
            INSERT INTO cliente (nome_cliente, cpf_cnpj, data_nascimento, email, telefone, ativo)
            VALUES ('Outro', '00000000002', '1992-02-02', 'outro@example.com', '(11) 90000-0002', true)
            RETURNING id_cliente
        """, Integer.class);

        mockMvc.perform(get("/api/clientes/{id}", outroCliente))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void adminShouldDeactivateCliente() throws Exception {
        mockMvc.perform(post("/api/clientes/{id}/desativar", clienteIdVinculado))
                .andExpect(status().isNoContent());

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
}
