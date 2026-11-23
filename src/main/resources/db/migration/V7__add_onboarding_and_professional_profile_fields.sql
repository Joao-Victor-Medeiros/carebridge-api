ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS termos_aceitos_em TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS email_verificado BOOLEAN;

UPDATE usuarios
SET termos_aceitos_em = COALESCE(termos_aceitos_em, criado_em),
    email_verificado = COALESCE(email_verificado, FALSE);

ALTER TABLE usuarios
    ALTER COLUMN tipo_perfil SET NOT NULL,
    ALTER COLUMN termos_aceitos_em SET NOT NULL,
    ALTER COLUMN email_verificado SET DEFAULT FALSE,
    ALTER COLUMN email_verificado SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_email_lower
    ON usuarios (LOWER(email));

ALTER TABLE profissional_saude
    ADD COLUMN IF NOT EXISTS conselho VARCHAR(255),
    ADD COLUMN IF NOT EXISTS uf VARCHAR(2),
    ADD COLUMN IF NOT EXISTS numero_registro VARCHAR(255),
    ADD COLUMN IF NOT EXISTS especialidade VARCHAR(255),
    ADD COLUMN IF NOT EXISTS onde_atende VARCHAR(255);