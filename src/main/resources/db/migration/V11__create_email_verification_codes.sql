UPDATE usuarios
SET email_verificado = TRUE
WHERE tipo_perfil = 'PACIENTE';
CREATE TABLE codigos_verificacao_email (
    id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    codigo_hash VARCHAR(255) NOT NULL,
    finalidade VARCHAR(40) NOT NULL,
    criado_em TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expira_em TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    utilizado_em TIMESTAMP(6) WITH TIME ZONE,
    tentativas INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT pk_codigos_verificacao_email PRIMARY KEY (id),
    CONSTRAINT fk_codigos_verificacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX idx_codigos_verificacao_usuario_criado
    ON codigos_verificacao_email (usuario_id, finalidade, criado_em DESC);
