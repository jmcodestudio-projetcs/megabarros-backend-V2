-- Ajuste o prefixo/versão conforme sua sequência de migrations (ex.: V21__add_cliente_ativo_column.sql)

-- 1) Adiciona a coluna 'ativo' na tabela cliente sem restrição para atualizar os registros existentes
ALTER TABLE cliente
    ADD COLUMN IF NOT EXISTS ativo BOOLEAN;

-- 2) Define 'ativo = TRUE' para todos os registros atuais (segurado ativo por padrão)
UPDATE cliente
SET ativo = TRUE
WHERE ativo IS NULL;

-- 3) Torna a coluna obrigatória (NOT NULL) e define o DEFAULT para novos registros
ALTER TABLE cliente
    ALTER COLUMN ativo SET NOT NULL;

ALTER TABLE cliente
    ALTER COLUMN ativo SET DEFAULT TRUE;