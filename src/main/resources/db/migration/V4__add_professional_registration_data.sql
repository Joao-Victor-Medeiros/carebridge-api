ALTER TABLE profissional_saude
    ADD COLUMN IF NOT EXISTS conselho VARCHAR(255),
    ADD COLUMN IF NOT EXISTS uf VARCHAR(2),
    ADD COLUMN IF NOT EXISTS numero_registro VARCHAR(255),
    ADD COLUMN IF NOT EXISTS especialidade VARCHAR(255),
    ADD COLUMN IF NOT EXISTS onde_atende VARCHAR(255);

-- Token de compatibilidade do checksum histórico: f8gCm6