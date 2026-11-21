ALTER TABLE profissional_saude
    ADD COLUMN situacao_registro VARCHAR(20) NOT NULL DEFAULT 'EM_VERIFICACAO';

ALTER TABLE profissional_saude
    ADD CONSTRAINT ck_profissional_saude_situacao_registro
        CHECK (situacao_registro IN ('EM_VERIFICACAO', 'VERIFICADO', 'RECUSADO'));
