# Autenticação Template (Spring Boot)

Um template de autenticação moderno com Spring Boot 3, Java 21, JWT (JJWT), JPA/Hibernate, PostgreSQL, Flyway, Actuator e uma arquitetura limpa (ports/adapters/usecases/policies). Pronto para reutilização em outros projetos via GitHub Template.

## Sumário
- Visão Geral
- Arquitetura e Organização
- Fluxos Principais
- Configuração (Profiles, .env, YAML)
- Banco de Dados e Migrations (Flyway)
- Build, Testes e Execução
- Endpoints e Segurança
- Observabilidade (Actuator)
- Como reutilizar como Template no GitHub
- Próximos Passos

---

## Visão Geral
Este projeto oferece um ponto de partida para serviços de autenticação com:
- Geração/validação de tokens JWT (access/refresh)
- Política de senha, autorização por roles via AOP
- Persistência de refresh tokens
- Rate limiting de login (in-memory, customizável)
- Auditoria básica
- Migrações de banco (Flyway)
- Configuração centralizada via `.env` para desenvolvimento

Stack:
- Java 21
- Spring Boot 3.4.x (Web, Security, Validation, Data JPA, Actuator)
- PostgreSQL + Flyway
- JJWT (io.jsonwebtoken)

---

## Arquitetura e Organização
O projeto segue uma abordagem de arquitetura limpa, separando claramente camadas:

- `application/`
  - `usecase/`: Casos de uso (Authenticate, Refresh, ChangePassword), contendo regras de negócio.
  - `policy/`: Políticas (PasswordPolicy, AuthorizationPolicy) para validações transversais.
  - `port/`: Interfaces que definem contratos para adapters externos (ex: `TokenServicePort`).

- `adapters/`
  - `security/jwt/`: Implementação de `TokenServicePort` usando JJWT (`JwtTokenService`), filtros (`JwtAuthenticationFilter`).
  - `security/rate/`: Limitador de taxa para login.
  - `persistence/`: Repositórios JPA para entidades necessárias (refresh tokens, etc.).

- `infrastructure/`
  - `config/`: Configurações Spring (`AopConfig`, `PolicyConfig`, `EnvConfig`).
  - `web/filters/`: Filtros web para correlação e metadados de requisições.

- `resources/`
  - `application.yml`, `application-dev.yml`: Configuração da aplicação.
  - `db/migration/`: Arquivos SQL versionados para migrações (V1, V2, V4, V5...).

Principais classes de configuração:
- `AopConfig`: habilita AspectJ proxy e disponibiliza `AuthorizationPolicy` como bean.
- `PolicyConfig`: registra `PasswordPolicy` como bean.
- `EnvConfig`: carrega variáveis do arquivo `.env` automaticamente como System Properties (para uso com `@Value`).

Principais classes de segurança JWT:
- `JwtTokenService`: implementação de `TokenServicePort` usando JJWT; gera e valida tokens, com claims e expiração configuráveis.
- `JwtAuthenticationFilter`: filtro que valida JWT nos requests protegidos.

---

## Fluxos Principais

### Autenticação (Login)
1. Usuário envia credenciais.
2. Caso de uso `AuthenticateUseCase` valida senha via `PasswordPolicy` e autentica contra o repositório.
3. `JwtTokenService` gera `accessToken` (curta duração) e `refreshToken` (longa duração) com claims essenciais.
4. Opcional: refresh token armazenado/atualizado em tabela (migrations V2/V5).

### Refresh Token
1. Cliente envia `refreshToken` válido.
2. `RefreshTokenUseCase` valida o token, verifica jti, expiração e emite novo `accessToken`.

### Troca de Senha
1. `ChangePasswordUseCase` aplica `PasswordPolicy` (força de senha mínima e regras), atualiza credenciais.

### Autorização via AOP
- `AuthorizationPolicy` é injetada nos aspectos; valida roles/permissões em pontos de corte definidos.

---

## Configuração (Profiles, .env, YAML)

### Profiles
- `default`: usa `application.yml` com fallbacks.
- `dev`: usa `application-dev.yml` para desenvolvimento local.

### .env
O projeto carrega automaticamente variáveis do `.env` na raiz, via `EnvConfig`. Exemplo de `.env`:
```
POSTGRES_DB=megabarros
POSTGRES_USER=megabarros
POSTGRES_PASSWORD=megabarros
JWT_ISSUER=megabarros-v2
JWT_SECRET=change-me-change-me-change-me-change-me
JWT_ACCESS_EXP_SECONDS=900
JWT_REFRESH_EXP_SECONDS=1209600
JWT_AUDIENCE=megabarros-frontend
PORT=8080
```

### YAML
- `application.yml`: configura banco, JPA, Flyway, Actuator e `server.port`. Possui fallbacks para env vars.
- `application-dev.yml`: valores prontos para desenvolvimento local (sem JWT, pois agora vem do `.env`).

### Injeção de Config no Código
`JwtTokenService` injeta valores direto do `.env` via `@Value`:
```java
public JwtTokenService(
  @Value("${JWT_ISSUER}") String issuer,
  @Value("${JWT_AUDIENCE}") String audience,
  @Value("${JWT_SECRET}") String secret,
  @Value("${JWT_ACCESS_EXP_SECONDS}") long accessExpSeconds,
  @Value("${JWT_REFRESH_EXP_SECONDS}") long refreshExpSeconds)
```
Isso é habilitado por `EnvConfig`, que lê `.env` e injeta em `System.setProperty(...)`.

---

## Banco de Dados e Migrations (Flyway)
- Configurado via `spring.flyway.*` (locations: `classpath:db/migration`).
- Migrações existentes:
  - `V1__baseline.sql`
  - `V2__auth_refresh_tokens.sql`
  - `V4__audit_log.sql`
  - `V5__update_refresh_usuario.sql`
- Ao iniciar, Flyway valida e aplica migrações automaticamente.

Pré-requisito local:
- PostgreSQL acessível em `localhost:5432`, com banco/usuário/senha do `.env`.

---

## Build, Testes e Execução

### Requisitos
- Java 21
- Maven 3.9+
- PostgreSQL local

### Build
```powershell
cd "C:\Projetos JM Code Studio\Mega Barros\autenticacao-template"
mvn clean package -DskipTests
```

### Executar (Jar)
```powershell
$env:SPRING_PROFILES_ACTIVE='dev'
java -jar target/autenticacao-template-0.0.1-SNAPSHOT.jar
```

### Executar (Maven)
```powershell
mvn spring-boot:run '-Dspring-boot.run.arguments=--spring.profiles.active=dev'
```

### Testes
- Há testes unitários para políticas e casos de uso em `src/test/java/...`
- Para executar:
```powershell
mvn test
```

---

## Endpoints e Segurança

- Filtro `JwtAuthenticationFilter` protege endpoints conforme configuração de Security.
- Geração/validação de JWT:
  - Access tokens: claim `typ=access`, `email`, `role`, `sub=userId`, `iss`, `aud`, `exp`.
  - Refresh tokens: claim `typ=refresh`, `jti`, longa duração.
- Handlers convertem `JwtException` em `TokenInvalidException` e retornos HTTP apropriados (geralmente 401).

Sugestão de endpoints (dependendo do seu controller):
- `POST /auth/login` -> retorna `accessToken` e `refreshToken`
- `POST /auth/refresh` -> retorna novo `accessToken`
- `POST /auth/change-password` -> troca senha

---

## Observabilidade (Actuator)
- Actuator expõe endpoints:
  - `/actuator/health`
  - `/actuator/info`
  - `/actuator/metrics`
  - `/actuator/prometheus`
- Em `application.yml`, configuração de exposição web já incluída.

---

## Como reutilizar como Template no GitHub

1. Suba este repositório ao GitHub.
2. Nas configurações do repositório, marque como "Template repository".
3. Em novos projetos, clique em "Use this template" e crie um repositório.
4. Ajuste:
   - Nome do app em `application.yml`
   - Variáveis de `.env` conforme seu contexto
   - Migrações e entidades de acordo com sua necessidade
   - Controllers/Endpoints específicos do seu domínio

Boas práticas ao reutilizar:
- Mantenha `EnvConfig` para desenvolvimento local simples.
- Em produção, prefira variáveis de ambiente do sistema ou Secrets (Vault, AWS Secrets Manager, etc.).
- Atualize `PasswordPolicy` e `AuthorizationPolicy` para regras do seu negócio.

---

## Arquitetura e pacotes usados:

- application
    - domain: `corretor/Corretor`
    - port
        - in: casos de uso (interfaces)
        - out: `CorretorRepositoryPort` (já existe `CurrentUserPort` reutilizado)
    - usecase: `CorretorUseCasesImpl` (orquestra os ports)
- adapters
    - persistence.jpa
        - entity: `CorretorEntity`
        - repository: `CorretorJpaRepository`
        - adapter: `CorretorRepositoryAdapter` (implementa `CorretorRepositoryPort`)
        - mapper: `CorretorPersistenceMapper` (MapStruct)
    - web
        - controller: `CorretorController`
        - dto: `web/dto/corretor/*`
        - mapper: `CorretorWebMapper` (MapStruct)
- resources
    - db/migration: `V6__update_corretor_usuario_bigint.sql` (se necessário, para alinhar FK ao `usuario.id_usuario BIGINT`)

Observações:
- Reutiliza `CurrentUserPort` existente: `Long userId(), String email(), String role()`.
- Usa `Long` para `usuarioId` no domínio e persistência. O `idCorretor` continua `Integer` (como no baseline).
- PreAuthorize mantém `hasRole('CORRETOR')` (Spring adiciona prefixo `ROLE_`).

---

## Licença
Este template foi criado para servir de base em projetos internos. Ajuste a licença conforme necessário quando publicar no GitHub.

