-- Alinha tipo do FK corretor.id_usuario ao BIGINT (int8), compatível com V5__update_refresh_usuario.sql
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'corretor'
              AND column_name = 'id_usuario'
              AND data_type IN ('integer')
        ) THEN
            ALTER TABLE public.corretor
                ALTER COLUMN id_usuario TYPE int8 USING id_usuario::int8;

            -- Recria a FK para garantir consistência com usuario(id_usuario BIGINT)
            ALTER TABLE public.corretor
                DROP CONSTRAINT IF EXISTS fk_corretor_usuario,
                ADD CONSTRAINT fk_corretor_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);
        END IF;
    END$$;