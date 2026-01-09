# Política de Segurança

- Não comitar segredos (JWT_SECRET, senhas).
- Variáveis sensíveis via ambiente ou secret manager.
- Tokens:
  - Access curto (padrão: 15 min)
  - Refresh persistido (hash), rotacionado a cada uso, revogado na troca de senha.
  - Enforce de issuer/audience.
- Auditoria:
  - Registro de LOGIN_SUCCESS/FAILED, REFRESH_SUCCESS/FAILED, PASSWORD_CHANGE_SUCCESS/FAILED, RATE_LIMIT.
  - Mascarar PII conforme necessário.
- Rate limiting:
  - Adapter in-memory por padrão; considerar Redis em produção.
- Actuator:
  - `/actuator/health` público; demais endpoints restritos a `ADMIN`.
- Considerar RS256/ES256 com rotação de chaves em produção (KMS/JWKS).
