CREATE TEMP TABLE paciente_id_map ON COMMIT DROP AS
SELECT id AS id_antigo, usuario_id AS id_usuario
FROM pacientes;

CREATE TEMP TABLE profissional_id_map ON COMMIT DROP AS
SELECT id AS id_antigo, usuario_id AS id_usuario
FROM profissional_saude;

DO $$
BEGIN
    IF EXISTS (
        SELECT id_usuario
        FROM paciente_id_map
        GROUP BY id_usuario
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Migration interrompida: mais de um paciente referencia o mesmo usuário';
    END IF;

    IF EXISTS (
        SELECT id_usuario
        FROM profissional_id_map
        GROUP BY id_usuario
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Migration interrompida: mais de um profissional referencia o mesmo usuário';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM paciente_id_map AS mapa
        LEFT JOIN usuarios AS usuario ON usuario.id = mapa.id_usuario
        WHERE usuario.id IS NULL
    ) THEN
        RAISE EXCEPTION 'Migration interrompida: existem pacientes sem usuário correspondente';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM profissional_id_map AS mapa
        LEFT JOIN usuarios AS usuario ON usuario.id = mapa.id_usuario
        WHERE usuario.id IS NULL
    ) THEN
        RAISE EXCEPTION 'Migration interrompida: existem profissionais sem usuário correspondente';
    END IF;
END
$$;

CREATE TEMP TABLE referencias_perfis (
    tabela_pai TEXT NOT NULL,
    esquema_filho TEXT NOT NULL,
    tabela_filho TEXT NOT NULL,
    coluna_filho TEXT NOT NULL,
    constraint_filho TEXT NOT NULL
) ON COMMIT DROP;

INSERT INTO referencias_perfis (
    tabela_pai,
    esquema_filho,
    tabela_filho,
    coluna_filho,
    constraint_filho
)
SELECT
    tabela_pai.relname,
    esquema_filho.nspname,
    tabela_filho.relname,
    coluna_filho.attname,
    constraint_filho.conname
FROM pg_constraint AS constraint_filho
JOIN pg_class AS tabela_pai
    ON tabela_pai.oid = constraint_filho.confrelid
JOIN pg_class AS tabela_filho
    ON tabela_filho.oid = constraint_filho.conrelid
JOIN pg_namespace AS esquema_filho
    ON esquema_filho.oid = tabela_filho.relnamespace
JOIN pg_attribute AS coluna_filho
    ON coluna_filho.attrelid = constraint_filho.conrelid
   AND coluna_filho.attnum = constraint_filho.conkey[1]
WHERE constraint_filho.contype = 'f'
  AND constraint_filho.confrelid IN ('pacientes'::regclass, 'profissional_saude'::regclass)
  AND array_length(constraint_filho.conkey, 1) = 1
  AND array_length(constraint_filho.confkey, 1) = 1;

DO $$
DECLARE
    referencia RECORD;
    constraint_perfil RECORD;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint AS constraint_filho
        WHERE constraint_filho.contype = 'f'
          AND constraint_filho.confrelid IN ('pacientes'::regclass, 'profissional_saude'::regclass)
          AND (
              array_length(constraint_filho.conkey, 1) <> 1
              OR array_length(constraint_filho.confkey, 1) <> 1
          )
    ) THEN
        RAISE EXCEPTION 'Migration interrompida: foi encontrada referência composta para um perfil';
    END IF;

    FOR referencia IN SELECT * FROM referencias_perfis LOOP
        EXECUTE format(
            'ALTER TABLE %I.%I DROP CONSTRAINT %I',
            referencia.esquema_filho,
            referencia.tabela_filho,
            referencia.constraint_filho
        );
    END LOOP;

    FOR referencia IN SELECT * FROM referencias_perfis LOOP
        IF referencia.tabela_pai = 'pacientes' THEN
            EXECUTE format(
                'UPDATE %I.%I AS filho '
                'SET %I = mapa.id_usuario '
                'FROM paciente_id_map AS mapa '
                'WHERE filho.%I = mapa.id_antigo',
                referencia.esquema_filho,
                referencia.tabela_filho,
                referencia.coluna_filho,
                referencia.coluna_filho
            );
        ELSE
            EXECUTE format(
                'UPDATE %I.%I AS filho '
                'SET %I = mapa.id_usuario '
                'FROM profissional_id_map AS mapa '
                'WHERE filho.%I = mapa.id_antigo',
                referencia.esquema_filho,
                referencia.tabela_filho,
                referencia.coluna_filho,
                referencia.coluna_filho
            );
        END IF;
    END LOOP;

    FOR constraint_perfil IN
        SELECT conrelid::regclass::text AS tabela, conname
        FROM pg_constraint
        WHERE conrelid IN ('pacientes'::regclass, 'profissional_saude'::regclass)
          AND contype IN ('p', 'f', 'u')
    LOOP
        EXECUTE format(
            'ALTER TABLE %I DROP CONSTRAINT %I',
            constraint_perfil.tabela,
            constraint_perfil.conname
        );
    END LOOP;
END
$$;

UPDATE pacientes AS paciente
SET id = mapa.id_usuario
FROM paciente_id_map AS mapa
WHERE paciente.id = mapa.id_antigo;

UPDATE profissional_saude AS profissional
SET id = mapa.id_usuario
FROM profissional_id_map AS mapa
WHERE profissional.id = mapa.id_antigo;

ALTER TABLE pacientes
    ADD CONSTRAINT pk_pacientes PRIMARY KEY (id),
    ADD CONSTRAINT uk_pacientes_usuario UNIQUE (usuario_id),
    ADD CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id);

ALTER TABLE profissional_saude
    ADD CONSTRAINT pk_profissional_saude PRIMARY KEY (id),
    ADD CONSTRAINT uk_profissional_saude_usuario UNIQUE (usuario_id),
    ADD CONSTRAINT fk_profissional_saude_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id);

DO $$
DECLARE
    referencia RECORD;
BEGIN
    FOR referencia IN SELECT * FROM referencias_perfis LOOP
        EXECUTE format(
            'ALTER TABLE %I.%I ADD CONSTRAINT %I FOREIGN KEY (%I) REFERENCES %I (id)',
            referencia.esquema_filho,
            referencia.tabela_filho,
            referencia.constraint_filho,
            referencia.coluna_filho,
            referencia.tabela_pai
        );
    END LOOP;
END
$$;