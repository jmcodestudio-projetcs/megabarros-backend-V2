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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc // filtros ATIVOS
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class UsuarioApiITest {

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
    @Autowired TokenServicePort tokens;

    Long adminId;
    Long usuarioId;
    String adminToken;
    String usuarioToken;

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
            VALUES ('Admin', 'admin@example.com', 'x', 'ADMIN', true, false)
            RETURNING id_usuario
        """, Long.class);

        usuarioId = jdbc.queryForObject("""
            INSERT INTO usuario (nome_usuario, email, senha_hash, perfil_usuario, ativo, must_change_password)
            VALUES ('User', 'user@example.com', 'x', 'USUARIO', true, false)
            RETURNING id_usuario
        """, Long.class);

        assertThat(adminId).isNotNull();
        assertThat(usuarioId).isNotNull();

        adminToken = tokens.generateAccessToken(adminId, "admin@example.com", "ADMIN", Map.of(), Instant.now());
        usuarioToken = tokens.generateAccessToken(usuarioId, "user@example.com", "USUARIO", Map.of(), Instant.now());
    }

    private String jsonUsuario(String nome, String email) throws Exception {
        Map<String, Object> body = Map.of(
                "nome", nome,
                "email", email,
                "senha", "StrongPass@2025",
                "perfil", "USUARIO",
                "ativo", true,
                "mustChangePassword", false
        );
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @Order(1)
    void shouldCreateUsuarioAsAdmin() throws Exception {
        String body = jsonUsuario("Novo User", "novo@example.com");

        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").isNumber())
                .andExpect(jsonPath("$.nome").value("Novo User"))
                .andExpect(jsonPath("$.email").value("novo@example.com"))
                .andExpect(jsonPath("$.perfil").value("USUARIO"));
    }

    @Test
    @Order(2)
    void shouldForbidCreateAsUsuario() throws Exception {
        String body = jsonUsuario("User Bloqueado", "bloq@example.com");

        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void shouldListUsuariosAsAdmin() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario").isNumber());
    }

    @Test
    @Order(4)
    void shouldGetUsuarioAsUsuario() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", usuarioId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + usuarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(usuarioId))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    @Order(5)
    void shouldUpdateAndDeleteAsAdmin() throws Exception {
        String updateBody = objectMapper.writeValueAsString(Map.of(
                "nome", "User Editado",
                "email", "user.editado@example.com",
                "senha", "StrongPass@2026",
                "perfil", "USUARIO",
                "ativo", true,
                "mustChangePassword", false
        ));

        mockMvc.perform(put("/api/usuarios/{id}", usuarioId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("User Editado"))
                .andExpect(jsonPath("$.email").value("user.editado@example.com"));

        mockMvc.perform(delete("/api/usuarios/{id}", usuarioId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/{id}", usuarioId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}