ALTER TABLE oportunidades
    ADD COLUMN especialidade VARCHAR(60),
    ADD COLUMN modalidade_acesso VARCHAR(20),
    ADD COLUMN preco NUMERIC(10, 2),
    ADD COLUMN publico_alvo_observacao TEXT,
    ADD COLUMN vagas_totais INTEGER,
    ADD COLUMN vagas_preenchidas INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN cep VARCHAR(9),
    ADD COLUMN endereco VARCHAR(255),
    ADD COLUMN cidade VARCHAR(120),
    ADD COLUMN uf VARCHAR(2),
    ADD COLUMN inscricoes_de DATE,
    ADD COLUMN inscricoes_ate DATE,
    ADD COLUMN criterios_elegibilidade TEXT,
    ADD COLUMN sem_restricoes BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN forma_contato VARCHAR(20),
    ADD COLUMN prazo_resposta_dias INTEGER NOT NULL DEFAULT 15,
    ADD COLUMN status VARCHAR(20);

-- O modelo antigo guardava texto livre e um booleano. Nenhuma oportunidade antiga
-- tem os campos que publicar exige (vagas, modalidade de acesso, periodo), entao
-- todas voltam a rascunho e o texto livre vai para os campos de observacao.
UPDATE oportunidades
SET status = 'RASCUNHO',
    criterios_elegibilidade = condicoes_acceso,
    publico_alvo_observacao = publico_alvo,
    publico_alvo = NULL,
    tipo_servico = CASE WHEN UPPER(tipo_servico) IN ('CONSULTA', 'EXAME', 'TERAPIA', 'PROCEDIMENTO', 'GRUPO')
                        THEN UPPER(tipo_servico) END,
    modalidade = CASE WHEN UPPER(modalidade) IN ('PRESENCIAL', 'ONLINE', 'HIBRIDO')
                      THEN UPPER(modalidade) END;

ALTER TABLE oportunidades
    ALTER COLUMN status SET NOT NULL,
    DROP COLUMN status_ativa,
    DROP COLUMN condicoes_acceso,
    ADD CONSTRAINT ck_oportunidades_status CHECK (status IN ('RASCUNHO', 'ATIVA', 'ENCERRADA')),
    ADD CONSTRAINT ck_oportunidades_vagas CHECK (vagas_preenchidas >= 0
        AND (vagas_totais IS NULL OR vagas_preenchidas <= vagas_totais)),
    ADD CONSTRAINT ck_oportunidades_preco CHECK (preco IS NULL OR preco > 0);

CREATE INDEX ix_oportunidades_status_atualizacao ON oportunidades (status, ultima_atualizacao DESC);
CREATE INDEX ix_oportunidades_ofertante ON oportunidades (ofertante_id);

ALTER TABLE formulario_triagem
    ADD COLUMN modo VARCHAR(20) NOT NULL DEFAULT 'PADRAO',
    ADD COLUMN perguntas TEXT,
    ADD COLUMN documentos_exigidos TEXT,
    DROP COLUMN configuracao_campos,
    DROP COLUMN ofertante_id;

ALTER TABLE formulario_triagem
    ADD CONSTRAINT ck_formulario_triagem_modo CHECK (modo IN ('PADRAO', 'PERSONALIZADO'));
