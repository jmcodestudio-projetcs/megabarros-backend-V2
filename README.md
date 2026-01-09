# Spring Boot Hex Auth Template

Template para projetos Spring Boot (Java 21) com arquitetura hexagonal e autenticação/segurança robustas:
- JWT access (curto) + refresh token persistido e rotacionado (Postgres/Flyway)
- Enforce de issuer/audience e claims `typ` (access/refresh)
- Revogação de refresh tokens na troca de senha
- Rate limiting + lockout (adapter in-memory; pronto p/ Redis)
- Auditoria (audit_log) com IP/User-Agent e metadados
- Authorization Policy central com anotações `@RequireRole` / `@RequireAnyRole` e Aspect
- Correlation ID por request (MDC, `X-Correlation-ID`)
- Actuator restrito: `/actuator/health` público, demais apenas para `ADMIN`
- Testes unitários dos casos de uso e adapters principais
- Arquitetura hexagonal (Ports & Adapters): use cases independentes de infra

## Requisitos
- Java 21
- Maven 3.9+
- Docker Compose (para Postgres 15)
- GitHub Actions (opcional, já com workflow de CI)

## Quickstart
1. Clique em "Use this template" no GitHub e crie seu repositório.
2. Clone o repo e suba o Postgres:
   ```bash
   docker-compose up -d
   ```
3. Configure variáveis de ambiente (ex.: `JWT_SECRET`, `DB_URL`, `DB_USER`, `DB_PASS`).
4. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
5. Rode os testes:
   ```bash
   ./mvnw test
   ```

## Estrutura (resumo)
```
src/main/java/...
  application/           # casos de uso, ports in/out, policies
  adapters/
    web/                 # controllers, DTOs, exception handlers
    security/            # JWT, rate limit, current user, AOP auth
    persistence/jpa/     # entidades JPA, repos Spring Data, adapters
  infrastructure/        # config beans, observability (correlation)
src/main/resources/
  application.yml
  db/migration/          # V1 baseline, V2 refresh tokens, V3 audit
src/test/java/           # testes unitários (use cases, JWT, rate, policy)
```

## Personalização
- Altere `security.jwt.issuer`, `security.jwt.audience` e expirações em `application.yml`.
- Ajuste roles e mapeamento no `JwtAuthenticationFilter` (`ROLE_ADMIN`, etc.).
- Política de senha (`PasswordPolicy`): tamanho mínimo, complexidade, lista bloqueada.
- Rate limiter: troque adapter in-memory por Redis (ex.: `redisson`).
- Auditoria: revise retenção, índices e metadados conforme LGPD/GDPR.

## Bootstrap (renomear namespace)
Execute o script de bootstrap (Linux/Mac):
```bash
scripts/bootstrap.sh
```
Ele irá:
- Pedir novo `groupId`, `artifactId` e `package base`
- Atualizar `pom.xml`
- Renomear pacotes no `src/main/java` e `src/test/java`

Para Windows, use `scripts/bootstrap.ps1`.

## Segurança
- Não comitar segredos (`JWT_SECRET`, senhas); use env vars.
- Considere RS256/ES256 + rotação de chaves em produção (KMS/JWKS).
- Adicione MFA/2FA para endpoints sensíveis, se necessário.

## Licença
Este template é disponibilizado sob a licença MIT (ver arquivo LICENSE).
