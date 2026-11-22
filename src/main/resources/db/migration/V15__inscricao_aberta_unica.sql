-- Antes do indice, cancela solicitacoes abertas repetidas, mantendo a mais antiga.
UPDATE inscricoes AS i
SET status = 'CANCELADA',
    decidida_em = NOW()
WHERE i.status IN ('RECEBIDA', 'EM_ANALISE', 'AGUARDANDO_INFORMACOES')
  AND EXISTS (
      SELECT 1
      FROM inscricoes AS j
      WHERE j.paciente_id = i.paciente_id
        AND j.oportunidade_id = i.oportunidade_id
        AND j.status IN ('RECEBIDA', 'EM_ANALISE', 'AGUARDANDO_INFORMACOES')
        AND (j.data_inscricao < i.data_inscricao OR (j.data_inscricao = i.data_inscricao AND j.id < i.id))
  );

CREATE UNIQUE INDEX uk_inscricoes_aberta_por_paciente
    ON inscricoes (paciente_id, oportunidade_id)
    WHERE status IN ('RECEBIDA', 'EM_ANALISE', 'AGUARDANDO_INFORMACOES');
