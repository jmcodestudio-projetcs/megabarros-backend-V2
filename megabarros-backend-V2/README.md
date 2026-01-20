# MegaBarros Backend V2

Backend Java Spring Boot para autenticação e gestão de Seguradora, Produto, Apólice e Cliente.

## Requisitos
- Java 21
- Maven 3.9+
- Docker (para Testcontainers)

## Perfis e Segurança
- Perfis: ADMIN, USUARIO, CORRETOR
- Autenticação: JWT
- Filtro: `JwtAuthenticationFilter` popula `SecurityContext` com principal contendo `userId`, `email`, `role`.

## Variáveis JWT (testes e dev)
Defina via propriedades ou `@DynamicPropertySource`:
- `JWT_ISSUER=megabarros-v2`
- `JWT_AUDIENCE=megabarros-frontend`
- `JWT_SECRET=test-secret-32-bytes-minimum-1234567890`
- `JWT_ACCESS_EXP_SECONDS=3600`
- `JWT_REFRESH_EXP_SECONDS=1209600`

## Build e Testes
- Compilar: `mvn clean compile`
- Testes: `mvn clean test`
    - Testcontainers irá subir PostgreSQL em container
    - Migrations (Flyway) são aplicadas no schema `public`

## Execução local
- `mvn spring-boot:run`
- Backend: `http://localhost:8080`
- Rotas principais:
    - `POST /auth/login` (gera JWT)
    - Seguradora:
        - `GET /api/seguradoras`
        - `POST /api/seguradoras` (ADMIN/USUARIO)
        - `PUT /api/seguradoras/{id}` (ADMIN/USUARIO)
        - `DELETE /api/seguradoras/{id}` (ADMIN/USUARIO)
        - `POST /api/seguradoras/{id}/produtos` (ADMIN/USUARIO)
        - `DELETE /api/seguradoras/produtos/{id}` (ADMIN/USUARIO)
    - Apólice:
        - `GET /api/apolices`
        - `GET /api/apolices/{id}`
        - `POST /api/apolices` (ADMIN/USUARIO)
        - `PUT /api/apolices/{id}` (ADMIN/USUARIO)
        - `POST /api/apolices/{id}/cancel` (ADMIN/USUARIO)
        - `POST /api/apolices/{id}/parcelas` (ADMIN/USUARIO)
        - `POST /api/apolices/parcelas/{id}/pagar` (ADMIN/USUARIO)
    - Cliente:
        - `GET /api/clientes`
        - `GET /api/clientes/{id}`
        - `POST /api/clientes` (ADMIN/USUARIO)
        - `PUT /api/clientes/{id}` (ADMIN/USUARIO/CORRETOR: apenas contato pelo corretor)
        - `POST /api/clientes/{id}/desativar` (ADMIN/USUARIO)

## Exceções e validações
- 403: `AccessDeniedException`
- 409: `IllegalStateException` (conflitos, duplicidades, bloqueios)
- 400: `MethodArgumentNotValidException`, `ConstraintViolationException`, `IllegalArgumentException`, `HttpMessageNotReadableException`

## Swagger (opcional)
Para habilitar documentação:
1. Adicione a dependência:
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.5.0</version>
</dependency>
```
2. Acesse `http://localhost:8080/swagger-ui.html`

## Auditoria e Logs
- Use cases registram logs com `actor` (username), `role`, IDs e resultado das operações.
- Sem vazamento de dados sensíveis (senhas, tokens, etc).

## Postman
- Coleções disponíveis em `*.postman_collection.json` para Seguradora/Produto, Apólice e Cliente.