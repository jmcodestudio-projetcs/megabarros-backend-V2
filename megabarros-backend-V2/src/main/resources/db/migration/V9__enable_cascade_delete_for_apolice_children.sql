-- 1) Status da apólice
ALTER TABLE IF EXISTS apolice_status
    DROP CONSTRAINT IF EXISTS fk_status_apolice;

ALTER TABLE apolice_status
    ADD CONSTRAINT fk_status_apolice
        FOREIGN KEY (id_apolice) REFERENCES apolice(id_apolice)
            ON DELETE CASCADE;

-- 2) Parcelas da apólice
ALTER TABLE IF EXISTS parcela_apolice
    DROP CONSTRAINT IF EXISTS fk_parcela_apolice;

ALTER TABLE parcela_apolice
    ADD CONSTRAINT fk_parcela_apolice
        FOREIGN KEY (id_apolice) REFERENCES apolice(id_apolice)
            ON DELETE CASCADE;

-- 3) Coberturas da apólice (se aplicável)
ALTER TABLE IF EXISTS apolice_cobertura
    DROP CONSTRAINT IF EXISTS fk_cobertura_apolice;

ALTER TABLE apolice_cobertura
    ADD CONSTRAINT fk_cobertura_apolice
        FOREIGN KEY (id_apolice) REFERENCES apolice(id_apolice)
            ON DELETE CASCADE;

-- 4) Beneficiários da apólice (se aplicável)
ALTER TABLE IF EXISTS beneficiario
    DROP CONSTRAINT IF EXISTS fk_beneficiario_apolice;

ALTER TABLE beneficiario
    ADD CONSTRAINT fk_beneficiario_apolice
        FOREIGN KEY (id_apolice) REFERENCES apolice(id_apolice)
            ON DELETE CASCADE;