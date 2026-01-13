-- Schema baseline for megabarros (PostgreSQL 15)

-- CLIENTE
CREATE TABLE IF NOT EXISTS public.cliente (
                                              id_cliente INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              nome_cliente VARCHAR(150) NOT NULL,
                                              genero VARCHAR(30),
                                              nacionalidade VARCHAR(80),
                                              identidade VARCHAR(30),
                                              orgao_emissor VARCHAR(30),
                                              data_emissao_identidade DATE,
                                              categoria_profissional VARCHAR(80),
                                              ocupacao_principal VARCHAR(100),
                                              cpf_cnpj VARCHAR(18) UNIQUE,
                                              data_nascimento DATE,
                                              residente_brasil BOOLEAN DEFAULT TRUE,
                                              estado_civil VARCHAR(50),
                                              filhos INT DEFAULT 0,
                                              renda_mensal_bruta NUMERIC(14,2),
                                              data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              email VARCHAR(150),
                                              telefone VARCHAR(30)
);

-- USUARIO
CREATE TABLE IF NOT EXISTS public.usuario (
                                              id_usuario INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              nome_usuario VARCHAR(100) NOT NULL,
                                              email VARCHAR(150) NOT NULL UNIQUE,
                                              senha_hash VARCHAR(255) NOT NULL,
                                              perfil_usuario VARCHAR(50),
                                              ativo BOOLEAN DEFAULT TRUE,
                                              data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              must_change_password BOOLEAN DEFAULT FALSE NOT NULL
);

-- CLIENTE_ENDERECO
CREATE TABLE IF NOT EXISTS public.cliente_endereco (
                                                       id_endereco INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                       id_cliente INT NOT NULL,
                                                       cep VARCHAR(10),
                                                       bairro VARCHAR(100),
                                                       uf CHAR(2),
                                                       logradouro VARCHAR(150),
                                                       numero VARCHAR(30),
                                                       cidade VARCHAR(100),
                                                       complemento VARCHAR(150),
                                                       tipo_endereco VARCHAR(50) DEFAULT 'principal',
                                                       CONSTRAINT fk_endereco_cliente FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente)
);
CREATE INDEX IF NOT EXISTS idx_endereco_cliente ON public.cliente_endereco(id_cliente);

-- SEGURADORA
CREATE TABLE IF NOT EXISTS public.seguradora (
                                                 id_seguradora INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                 nome_seguradora VARCHAR(100) NOT NULL UNIQUE
);

-- CORRETOR
CREATE TABLE IF NOT EXISTS public.corretor (
                                               id_corretor INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                               id_usuario INT,
                                               nome_corretor VARCHAR(150) NOT NULL,
                                               corretora VARCHAR(150),
                                               cpf_cnpj VARCHAR(18),
                                               susep_pj VARCHAR(50),
                                               email VARCHAR(150),
                                               telefone VARCHAR(20),
                                               uf CHAR(2),
                                               data_nascimento DATE,
                                               data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                               susep_pf VARCHAR(50),
                                               doc VARCHAR(1000),
                                               CONSTRAINT fk_corretor_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario)
);
CREATE INDEX IF NOT EXISTS idx_corretor_usuario ON public.corretor(id_usuario);

-- CORRETOR_CLIENTE
CREATE TABLE IF NOT EXISTS public.corretor_cliente (
                                                       id_corretor_cliente INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                       id_corretor INT NOT NULL,
                                                       id_cliente INT NOT NULL,
                                                       data_inicio DATE DEFAULT CURRENT_DATE,
                                                       CONSTRAINT uq_corretor_cliente UNIQUE (id_corretor, id_cliente),
                                                       CONSTRAINT fk_cc_cliente FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente),
                                                       CONSTRAINT fk_cc_corretor FOREIGN KEY (id_corretor) REFERENCES public.corretor(id_corretor)
);
CREATE INDEX IF NOT EXISTS idx_cc_corretor ON public.corretor_cliente(id_corretor);
CREATE INDEX IF NOT EXISTS idx_cc_cliente ON public.corretor_cliente(id_cliente);

-- PRODUTO (unicidade por seguradora + nome)
CREATE TABLE IF NOT EXISTS public.produto (
                                              id_produto INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              nome_produto VARCHAR(100) NOT NULL,
                                              tipo_produto VARCHAR(50),
                                              id_seguradora INT NOT NULL,
                                              CONSTRAINT fk_produto_seguradora FOREIGN KEY (id_seguradora) REFERENCES public.seguradora(id_seguradora),
                                              CONSTRAINT uq_produto_nome_por_seguradora UNIQUE (id_seguradora, nome_produto)
);
CREATE INDEX IF NOT EXISTS idx_produto_seguradora ON public.produto(id_seguradora);

-- COBERTURA_PRODUTO
CREATE TABLE IF NOT EXISTS public.cobertura_produto (
                                                        id_cobertura INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                        id_produto INT NOT NULL,
                                                        tipo_cobertura VARCHAR(50) NOT NULL,
                                                        descricao VARCHAR(200),
                                                        valor_maximo NUMERIC(12,2),
                                                        CONSTRAINT fk_cobertura_produto FOREIGN KEY (id_produto) REFERENCES public.produto(id_produto)
);
CREATE INDEX IF NOT EXISTS idx_cobertura_produto_prod ON public.cobertura_produto(id_produto);

-- APOLICE
CREATE TABLE IF NOT EXISTS public.apolice (
                                              id_apolice INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              numero_apolice VARCHAR(50) NOT NULL,
                                              data_emissao DATE NOT NULL,
                                              vigencia_inicio DATE NOT NULL,
                                              vigencia_fim DATE NOT NULL,
                                              valor NUMERIC(10,2) NOT NULL,
                                              comissao_percentual NUMERIC(5,2) NOT NULL,
                                              tipo_contrato VARCHAR(50) NOT NULL,
                                              id_corretor_cliente INT NOT NULL,
                                              id_produto INT NOT NULL,
                                              id_seguradora INT NOT NULL,
                                              CONSTRAINT fk_apolice_corretor_cliente FOREIGN KEY (id_corretor_cliente) REFERENCES public.corretor_cliente(id_corretor_cliente),
                                              CONSTRAINT fk_apolice_produto FOREIGN KEY (id_produto) REFERENCES public.produto(id_produto),
                                              CONSTRAINT fk_apolice_seguradora FOREIGN KEY (id_seguradora) REFERENCES public.seguradora(id_seguradora),
                                              CONSTRAINT uq_apolice_numero UNIQUE (numero_apolice)
);
CREATE INDEX IF NOT EXISTS idx_apolice_cc ON public.apolice(id_corretor_cliente);
CREATE INDEX IF NOT EXISTS idx_apolice_produto ON public.apolice(id_produto);
CREATE INDEX IF NOT EXISTS idx_apolice_seguradora ON public.apolice(id_seguradora);

-- APOLICE_STATUS
CREATE TABLE IF NOT EXISTS public.apolice_status (
                                                     id_status INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                     id_apolice INT NOT NULL,
                                                     status VARCHAR(50) NOT NULL,
                                                     data_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                     data_fim TIMESTAMP,
                                                     CONSTRAINT fk_status_apolice FOREIGN KEY (id_apolice) REFERENCES public.apolice(id_apolice)
);
CREATE INDEX IF NOT EXISTS idx_apolice_status_apolice ON public.apolice_status(id_apolice);

-- PARCELA_APOLICE
CREATE TABLE IF NOT EXISTS public.parcela_apolice (
                                                      id_parcela INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                      id_apolice INT NOT NULL,
                                                      numero_parcela INT NOT NULL,
                                                      data_vencimento DATE NOT NULL,
                                                      valor_parcela NUMERIC(10,2) NOT NULL,
                                                      status_pagamento VARCHAR(50),
                                                      data_pagamento DATE,
                                                      CONSTRAINT fk_parcela_apolice FOREIGN KEY (id_apolice) REFERENCES public.apolice(id_apolice)
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_parcela_numero_por_apolice ON public.parcela_apolice(id_apolice, numero_parcela);
CREATE INDEX IF NOT EXISTS idx_parcela_apolice ON public.parcela_apolice(id_apolice);

-- APOLICE_COBERTURA
CREATE TABLE IF NOT EXISTS public.apolice_cobertura (
                                                        id_apolice_cobertura INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                        id_apolice INT NOT NULL,
                                                        id_cobertura INT NOT NULL,
                                                        valor_contratado NUMERIC(12,2),
                                                        CONSTRAINT fk_ac_apolice FOREIGN KEY (id_apolice) REFERENCES public.apolice(id_apolice),
                                                        CONSTRAINT fk_ac_cobertura FOREIGN KEY (id_cobertura) REFERENCES public.cobertura_produto(id_cobertura)
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_ac_por_apolice_cobertura ON public.apolice_cobertura(id_apolice, id_cobertura);

-- BENEFICIARIO
CREATE TABLE IF NOT EXISTS public.beneficiario (
                                                   id_beneficiario INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                   id_apolice INT NOT NULL,
                                                   id_cliente INT,
                                                   nome_beneficiario VARCHAR(150) NOT NULL,
                                                   cpf VARCHAR(14),
                                                   percentual_participacao NUMERIC(5,2),
                                                   CONSTRAINT fk_beneficiario_apolice FOREIGN KEY (id_apolice) REFERENCES public.apolice(id_apolice),
                                                   CONSTRAINT fk_beneficiario_cliente FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente)
);
CREATE INDEX IF NOT EXISTS idx_benef_apolice ON public.beneficiario(id_apolice);

-- CONTATO
CREATE TABLE IF NOT EXISTS public.contato (
                                              id_contato INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              id_cliente INT,
                                              id_corretor INT,
                                              telefone VARCHAR(20),
                                              email VARCHAR(150),
                                              autoriza_envio_email BOOLEAN,
                                              autoriza_envio_sms BOOLEAN,
                                              autoriza_envio_whatsapp BOOLEAN,
                                              criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              CONSTRAINT chk_contato_vinculo CHECK ((id_cliente IS NOT NULL) OR (id_corretor IS NOT NULL)),
                                              CONSTRAINT fk_contato_cliente FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente),
                                              CONSTRAINT fk_contato_corretor FOREIGN KEY (id_corretor) REFERENCES public.corretor(id_corretor)
);
CREATE INDEX IF NOT EXISTS idx_contato_cliente ON public.contato(id_cliente);
CREATE INDEX IF NOT EXISTS idx_contato_corretor ON public.contato(id_corretor);

-- NOTIFICACAO
CREATE TABLE IF NOT EXISTS public.notificacao (
                                                  id_notificacao INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                  id_cliente INT,
                                                  id_apolice INT,
                                                  id_parcela INT,
                                                  id_usuario INT,
                                                  tipo_notificacao VARCHAR(50) NOT NULL,
                                                  mensagem_notificacao TEXT NOT NULL,
                                                  data_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  status_envio VARCHAR(50) NOT NULL,
                                                  canal_comunicacao VARCHAR(50) NOT NULL,
                                                  CONSTRAINT fk_not_apolice FOREIGN KEY (id_apolice) REFERENCES public.apolice(id_apolice),
                                                  CONSTRAINT fk_not_cliente FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_cliente),
                                                  CONSTRAINT fk_not_parcela FOREIGN KEY (id_parcela) REFERENCES public.parcela_apolice(id_parcela),
                                                  CONSTRAINT fk_not_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario)
);
CREATE INDEX IF NOT EXISTS idx_not_apolice ON public.notificacao(id_apolice);
CREATE INDEX IF NOT EXISTS idx_not_cliente ON public.notificacao(id_cliente);
CREATE INDEX IF NOT EXISTS idx_not_parcela ON public.notificacao(id_parcela);
CREATE INDEX IF NOT EXISTS idx_not_usuario ON public.notificacao(id_usuario);