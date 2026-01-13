-- Cria a tabela para armazenar refresh tokens “com hash”, com suporte a rotação (replaced_by) e revogação (revoked_at, reason).

CREATE TABLE IF NOT EXISTS public.refresh_token (
                                                    id_refresh_token INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                    id_usuario INT NOT NULL,
                                                    token_hash VARCHAR(255) NOT NULL,
                                                    jti VARCHAR(64) NOT NULL,
                                                    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                    expires_at TIMESTAMP NOT NULL,
                                                    revoked_at TIMESTAMP NULL,
                                                    replaced_by VARCHAR(255) NULL,
                                                    reason VARCHAR(100) NULL,
                                                    CONSTRAINT fk_rt_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_token_hash ON public.refresh_token(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_token_usuario ON public.refresh_token(id_usuario);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires ON public.refresh_token(expires_at);