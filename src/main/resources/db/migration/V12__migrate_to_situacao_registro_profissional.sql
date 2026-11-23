DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'profissional_saude'
          AND column_name = 'status_verificacao'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'profissional_saude'
          AND column_name = 'situacao_registro'
    ) THEN
        ALTER TABLE profissional_saude
            RENAME COLUMN status_verificacao TO situacao_registro;
    END IF;
END $$;

ALTER TABLE profissional_saude
    ADD COLUMN IF NOT EXISTS situacao_registro VARCHAR(20);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'profissional_saude'
          AND column_name = 'status_verificacao'
    ) THEN
        UPDATE profissional_saude
        SET situacao_registro = COALESCE(situacao_registro, status_verificacao);

        ALTER TABLE profissional_saude
            DROP COLUMN status_verificacao;
    END IF;
END $$;

UPDATE profissional_saude
SET situacao_registro = 'EM_VERIFICACAO'
WHERE situacao_registro IS NULL;

ALTER TABLE profissional_saude
    ALTER COLUMN situacao_registro SET DEFAULT 'EM_VERIFICACAO',
    ALTER COLUMN situacao_registro SET NOT NULL,
    ADD CONSTRAINT ck_profissional_saude_situacao_registro
        CHECK (situacao_registro IN ('EM_VERIFICACAO', 'VERIFICADO', 'RECUSADO'));
