-- Ajuste o prefixo/versão conforme sua sequência (ex.: V22__alter_cliente_endereco_uf_to_varchar.sql)

-- Converte a coluna 'uf' de CHAR(2)/bpchar para VARCHAR(2)
ALTER TABLE cliente_endereco
    ALTER COLUMN uf TYPE varchar(2)
        USING uf::varchar(2);