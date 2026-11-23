ALTER TABLE profissional_saude
    ADD COLUMN IF NOT EXISTS status_verificacao VARCHAR(50);

UPDATE profissional_saude
SET status_verificacao = 'EM_VERIFICACAO'
WHERE status_verificacao IS NULL;

ALTER TABLE profissional_saude
    ALTER COLUMN status_verificacao SET DEFAULT 'EM_VERIFICACAO',
    ALTER COLUMN status_verificacao SET NOT NULL;