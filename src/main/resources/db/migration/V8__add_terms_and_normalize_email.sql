ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS accepted_terms BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS termos_aceitos_em TIMESTAMP WITH TIME ZONE;

UPDATE usuarios
SET email = LOWER(TRIM(email))
WHERE email IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_email_lower
    ON usuarios (LOWER(TRIM(email)));