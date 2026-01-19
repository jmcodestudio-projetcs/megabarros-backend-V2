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
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class CorretorApiITest {

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
        // Caso seu projeto use locations customizadas:
        // registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ObjectMapper objectMapper;

    Long usuarioId; // será criado antes de cada teste

    @BeforeEach
    void setup() {
        // Limpa dados (ordem importa por FKs)
        jdbc.update("DELETE FROM public.contato");
        jdbc.update("DELETE FROM public.apolice_cobertura");
        jdbc.update("DELETE FROM public.parcela_apolice");
        jdbc.update("DELETE FROM public.apolice_status");
        jdbc.update("DELETE FROM public.apolice");
        jdbc.update("DELETE FROM public.corretor_cliente");
        jdbc.update("DELETE FROM public.corretor");
        jdbc.update("DELETE FROM public.refresh_token");
        jdbc.update("DELETE FROM public.usuario");

        // Insere um usuário e captura o id gerado
        // perfil_usuario: 'admin' para permitir criar corretores em alguns testes
        usuarioId = jdbc.queryForObject(
                """
                INSERT INTO public.usuario (nome_usuario, email, senha_hash, perfil_usuario, ativo, must_change_password)
                VALUES (?, ?, ?, ?, true, false)
                RETURNING id_usuario
                """,
                Long.class,
                "Admin Teste", "admin.teste@example.com",
                // bcrypt de "password" (pode ser qualquer hash válido; não é usado no teste)
                "$2a$12$abcdefghijklmnopqrstuv..../abcdefghijklmno123456",
                "admin"
        );
        assertThat(usuarioId).isNotNull();
    }

    private String criarCorretorJson(Long idUsuario, String nome, String uf) throws Exception {
        Map<String, Object> body = Map.ofEntries(
                Map.entry("idUsuario", idUsuario),
                Map.entry("nomeCorretor", nome),
                Map.entry("corretora", "MegaBarros Corretora"),
                Map.entry("cpfCnpj", "12345678901"),
                Map.entry("susepPj", "PJ-123"),
                Map.entry("susepPf", "PF-456"),
                Map.entry("email", "corretor.teste@example.com"),
                Map.entry("telefone", "(11) 90000-0000"),
                Map.entry("uf", uf),
                Map.entry("dataNascimento", LocalDate.of(1985, 4, 20).toString()),
                Map.entry("doc", "DOC TESTE")
        );
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldCreateCorretorAsAdmin() throws Exception {
        String body = criarCorretorJson(usuarioId, "João da Silva", "SP");

        mockMvc.perform(post("/api/corretores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.idCorretor").isNumber())
                .andExpect(jsonPath("$.idUsuario").value(usuarioId))
                .andExpect(jsonPath("$.nomeCorretor").value("João da Silva"))
                .andExpect(jsonPath("$.uf").value("SP"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "corretor@example.com", roles = {"CORRETOR"})
    void shouldForbidCreateCorretorAsCorretor() throws Exception {
        String body = criarCorretorJson(usuarioId, "Maria Vendedora", "RJ");

        mockMvc.perform(post("/api/corretores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden()); // 403 devido à regra de negócio + @PreAuthorize
    }

    @Test
    @Order(3)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldListCorretores() throws Exception {
        // Arrange: cria um corretor
        String body = criarCorretorJson(usuarioId, "Pedro Listagem", "SP");
        mockMvc.perform(post("/api/corretores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isCreated());

        // Act + Assert: lista
        mockMvc.perform(get("/api/corretores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].idCorretor").exists());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldUpdateCorretor() throws Exception {
        // Arrange: cria
        String body = criarCorretorJson(usuarioId, "Carlos Atualizar", "SP");
        String location = mockMvc.perform(post("/api/corretores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).isNotBlank();
        String id = location.substring(location.lastIndexOf('/') + 1);

        // Act: atualiza
        String updateJson = objectMapper.writeValueAsString(Map.of(
                "nomeCorretor", "Carlos Atualizado",
                "telefone", "(11) 98888-7777",
                "uf", "RJ"
        ));
        mockMvc.perform(put("/api/corretores/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCorretor").value("Carlos Atualizado"))
                .andExpect(jsonPath("$.uf").value("RJ"));
    }

    @Test
    @Order(5)
    @WithMockUser(username = "admin.teste@example.com", roles = {"ADMIN"})
    void shouldDeleteCorretor() throws Exception {
        // Arrange: cria
        String body = criarCorretorJson(usuarioId, "Rafael Deletar", "SP");
        String location = mockMvc.perform(post("/api/corretores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        String id = location.substring(location.lastIndexOf('/') + 1);

        // Act: deleta
        mockMvc.perform(delete("/api/corretores/{id}", id))
                .andExpect(status().isNoContent());

        // Assert: buscar deve retornar 404
        mockMvc.perform(get("/api/corretores/{id}", id))
                .andExpect(status().isNotFound());
    }
}
