-- Tabela de auditoria de eventos de segurança e acesso

CREATE TABLE IF NOT EXISTS public.audit_log (
                                                id_audit BIGSERIAL PRIMARY KEY,
                                                occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                user_id BIGINT NULL,
                                                action VARCHAR(100) NOT NULL,       -- ex.: LOGIN_SUCCESS, LOGIN_FAILED, REFRESH_SUCCESS, PASSWORD_CHANGE
                                                subject VARCHAR(150) NULL,          -- ex.: email alvo, endpoint, recurso
                                                ip VARCHAR(64) NULL,
                                                user_agent VARCHAR(255) NULL,
                                                metadata JSONB NULL                 -- dados complementares seguros (NÃO armazenar senhas/tokens!)
);

CREATE INDEX IF NOT EXISTS idx_audit_log_user ON public.audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON public.audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_log_occurred ON public.audit_log(occurred_at);