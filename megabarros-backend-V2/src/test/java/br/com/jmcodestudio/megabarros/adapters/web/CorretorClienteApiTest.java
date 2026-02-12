package br.com.jmcodestudio.megabarros.adapters.web;

import br.com.jmcodestudio.megabarros.application.port.out.TokenServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CorretorClienteApiTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("megabarros_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.properties.hibernate.default_schema", () -> "public");
        r.add("spring.flyway.schemas", () -> "public");
        r.add("spring.flyway.defaultSchema", () -> "public");
        r.add("JWT_ISSUER", () -> "megabarros-v2");
        r.add("JWT_AUDIENCE", () -> "megabarros-frontend");
        r.add("JWT_SECRET", () -> "test-secret-32-bytes-minimum-1234567890");
        r.add("JWT_ACCESS_EXP_SECONDS", () -> "3600");
        r.add("JWT_REFRESH_EXP_SECONDS", () -> "1209600");
    }

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TokenServicePort tokens;

    Long adminId;
    Integer clienteId;
    Integer corretorId;
    Integer ccId;

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
        clienteId = jdbc.queryForObject("""
            INSERT INTO cliente (nome_cliente, cpf_cnpj, email, telefone, ativo)
            VALUES ('Cli', '00000000001', 'cli@example.com', '(11) 9', true) RETURNING id_cliente
        """, Integer.class);
        corretorId = jdbc.queryForObject("""
            INSERT INTO corretor (id_usuario, nome_corretor, uf)
            VALUES (?, 'Corretor 1', 'SP') RETURNING id_corretor
        """, Integer.class, adminId);
        ccId = jdbc.queryForObject("""
            INSERT INTO corretor_cliente (id_corretor, id_cliente)
            VALUES (?, ?) RETURNING id_corretor_cliente
        """, Integer.class, corretorId, clienteId);

        assertThat(adminId).isNotNull();
        assertThat(clienteId).isNotNull();
        assertThat(corretorId).isNotNull();
        assertThat(ccId).isNotNull();
    }

    @Test
    void shouldListCorretoresByCliente() throws Exception {
        String token = tokens.generateAccessToken(adminId, "admin@example.com", "ADMIN", java.util.Map.of(), Instant.now());

        mockMvc.perform(get("/api/clientes/{id}/corretores", clienteId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCorretorCliente").value(ccId))
                .andExpect(jsonPath("$[0].idCorretor").value(corretorId))
                .andExpect(jsonPath("$[0].idCliente").value(clienteId));
    }

    @Test
    void shouldResolveIdCorretorCliente() throws Exception {
        String token = tokens.generateAccessToken(adminId, "admin@example.com", "ADMIN", java.util.Map.of(), Instant.now());

        mockMvc.perform(get("/api/corretor-clientes/resolve")
                        .param("clienteId", String.valueOf(clienteId))
                        .param("corretorId", String.valueOf(corretorId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(ccId)));
    }
}
