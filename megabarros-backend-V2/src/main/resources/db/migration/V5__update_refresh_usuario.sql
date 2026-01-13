-- atualiza campo id_refresh_token para int8 e id_usuario para int8
ALTER TABLE public.refresh_token ALTER COLUMN id_refresh_token TYPE int8 USING id_refresh_token::int8;
ALTER TABLE public.refresh_token ALTER COLUMN id_usuario TYPE int8 USING id_usuario::int8;
ALTER TABLE public.usuario ALTER COLUMN id_usuario TYPE int8 USING id_usuario::int8;