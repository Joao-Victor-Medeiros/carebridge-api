CREATE TABLE usuarios (
    id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    tipo_perfil VARCHAR(255),
    criado_em TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    atualizado_em TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE TABLE pacientes (
    id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    telefone VARCHAR(255),
    endereco TEXT,
    usuario_id UUID NOT NULL,
    CONSTRAINT pk_pacientes PRIMARY KEY (id),
    CONSTRAINT uk_pacientes_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE profissional_saude (
    id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    documento_registro VARCHAR(4096),
    biografia_ou_descricao TEXT,
    validado_institucionalmente BOOLEAN NOT NULL,
    CONSTRAINT pk_profissional_saude PRIMARY KEY (id),
    CONSTRAINT uk_profissional_saude_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_profissional_saude_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE documentos_registro (
    profissional_id UUID NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo VARCHAR(255),
    arquivo BYTEA,
    CONSTRAINT fk_documentos_profissional FOREIGN KEY (profissional_id) REFERENCES profissional_saude (id)
);

CREATE TABLE oportunidades (
    id UUID NOT NULL,
    ofertante_id UUID NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    tipo_servico VARCHAR(255),
    publico_alvo TEXT,
    modalidade VARCHAR(255),
    condicoes_acceso TEXT,
    status_ativa BOOLEAN NOT NULL,
    ultima_atualizacao TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_oportunidades PRIMARY KEY (id),
    CONSTRAINT fk_oportunidades_ofertante FOREIGN KEY (ofertante_id) REFERENCES profissional_saude (id)
);

CREATE TABLE formulario_triagem (
    id UUID NOT NULL,
    oportunidade_id UUID NOT NULL,
    ofertante_id UUID NOT NULL,
    configuracao_campos TEXT,
    CONSTRAINT pk_formulario_triagem PRIMARY KEY (id),
    CONSTRAINT uk_formulario_oportunidade UNIQUE (oportunidade_id),
    CONSTRAINT fk_formulario_oportunidade FOREIGN KEY (oportunidade_id) REFERENCES oportunidades (id),
    CONSTRAINT fk_formulario_ofertante FOREIGN KEY (ofertante_id) REFERENCES profissional_saude (id)
);

CREATE TABLE inscricoes (
    id UUID NOT NULL,
    paciente_id UUID NOT NULL,
    oportunidade_id UUID NOT NULL,
    status VARCHAR(255) NOT NULL,
    dados_respostas_triagem TEXT,
    data_inscricao TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_inscricoes PRIMARY KEY (id),
    CONSTRAINT fk_inscricoes_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    CONSTRAINT fk_inscricoes_oportunidade FOREIGN KEY (oportunidade_id) REFERENCES oportunidades (id)
);