CREATE SEQUENCE inscricao_protocolo_seq START WITH 20001;

ALTER TABLE inscricoes
    ADD COLUMN protocolo VARCHAR(20),
    ADD COLUMN consentimento_dados_saude_em TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN prazo_resposta TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN decidida_em TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN motivo_decisao TEXT,
    ADD COLUMN pedido_informacao_texto TEXT,
    ADD COLUMN pedido_informacao_prazo TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN combinado_quando VARCHAR(255),
    ADD COLUMN combinado_onde VARCHAR(255),
    ADD COLUMN combinado_levar VARCHAR(255);

-- Inscricoes antigas nao tinham estado padronizado nem respostas em JSON.
UPDATE inscricoes AS i
SET protocolo = nextval('inscricao_protocolo_seq')::text,
    prazo_resposta = i.data_inscricao + make_interval(days => o.prazo_resposta_dias),
    status = CASE WHEN i.status IN ('RECEBIDA', 'EM_ANALISE', 'AGUARDANDO_INFORMACOES', 'APROVADA', 'RECUSADA', 'CANCELADA')
                  THEN i.status ELSE 'EM_ANALISE' END,
    dados_respostas_triagem = NULL
FROM oportunidades AS o
WHERE o.id = i.oportunidade_id;

ALTER TABLE inscricoes
    ALTER COLUMN protocolo SET NOT NULL,
    ALTER COLUMN prazo_resposta SET NOT NULL,
    ALTER COLUMN status TYPE VARCHAR(30),
    ADD CONSTRAINT uk_inscricoes_protocolo UNIQUE (protocolo),
    ADD CONSTRAINT ck_inscricoes_status CHECK (status IN
        ('RECEBIDA', 'EM_ANALISE', 'AGUARDANDO_INFORMACOES', 'APROVADA', 'RECUSADA', 'CANCELADA'));

CREATE INDEX ix_inscricoes_paciente ON inscricoes (paciente_id, data_inscricao DESC);
CREATE INDEX ix_inscricoes_oportunidade ON inscricoes (oportunidade_id, data_inscricao DESC);

CREATE TABLE inscricao_documentos (
    id UUID NOT NULL,
    inscricao_id UUID NOT NULL,
    documento_id VARCHAR(60),
    nome_arquivo VARCHAR(255) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    enviado_em TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_inscricao_documentos PRIMARY KEY (id),
    CONSTRAINT fk_inscricao_documentos_inscricao FOREIGN KEY (inscricao_id) REFERENCES inscricoes (id) ON DELETE CASCADE
);

CREATE INDEX ix_inscricao_documentos_inscricao ON inscricao_documentos (inscricao_id);

CREATE TABLE mensagens (
    id UUID NOT NULL,
    inscricao_id UUID NOT NULL,
    autor VARCHAR(20) NOT NULL,
    texto TEXT NOT NULL,
    enviada_em TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_mensagens PRIMARY KEY (id),
    CONSTRAINT fk_mensagens_inscricao FOREIGN KEY (inscricao_id) REFERENCES inscricoes (id) ON DELETE CASCADE,
    CONSTRAINT ck_mensagens_autor CHECK (autor IN ('PACIENTE', 'PROFISSIONAL_SAUDE'))
);

CREATE INDEX ix_mensagens_inscricao ON mensagens (inscricao_id, enviada_em);
