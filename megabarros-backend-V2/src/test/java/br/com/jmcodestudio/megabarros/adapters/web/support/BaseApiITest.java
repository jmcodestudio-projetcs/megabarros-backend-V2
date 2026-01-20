package br.com.jmcodestudio.megabarros.adapters.web.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc // filtros ATIVOS
public abstract class BaseApiITest {

    @DynamicPropertySource
    static void jwtProps(DynamicPropertyRegistry registry) {
        registry.add("JWT_ISSUER", () -> "megabarros-v2");
        registry.add("JWT_AUDIENCE", () -> "megabarros-frontend");
        registry.add("JWT_SECRET", () -> "test-secret-32-bytes-minimum-1234567890");
        registry.add("JWT_ACCESS_EXP_SECONDS", () -> "3600");
        registry.add("JWT_REFRESH_EXP_SECONDS", () -> "1209600");
    }
}
