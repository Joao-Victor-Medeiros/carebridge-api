-- Mantém a coluna legada durante a transição para que uma instância da versão
-- anterior continue funcionando enquanto o Render promove a nova instância.
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS nome VARCHAR(255);

ALTER TABLE profissional_saude
    ADD COLUMN IF NOT EXISTS nome VARCHAR(255);

UPDATE profissional_saude AS profissional
SET nome = usuario.nome
FROM usuarios AS usuario
WHERE profissional.usuario_id = usuario.id
  AND profissional.nome IS NULL
  AND usuario.nome IS NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM profissional_saude
        WHERE nome IS NULL OR BTRIM(nome) = ''
    ) THEN
        RAISE EXCEPTION
            'Migration interrompida: existem profissionais sem nome em profissional_saude e usuarios';
    END IF;
END
$$;

ALTER TABLE usuarios
    ALTER COLUMN nome DROP NOT NULL;

ALTER TABLE profissional_saude
    ALTER COLUMN nome SET NOT NULL;

CREATE OR REPLACE FUNCTION preencher_nome_profissional_legado()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.nome IS NULL OR BTRIM(NEW.nome) = '' THEN
        SELECT usuario.nome
        INTO NEW.nome
        FROM usuarios AS usuario
        WHERE usuario.id = NEW.usuario_id;
    END IF;

    IF NEW.nome IS NULL OR BTRIM(NEW.nome) = '' THEN
        RAISE EXCEPTION 'Nome do profissional é obrigatório';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_preencher_nome_profissional_legado ON profissional_saude;

CREATE TRIGGER trg_preencher_nome_profissional_legado
BEFORE INSERT OR UPDATE OF usuario_id, nome
ON profissional_saude
FOR EACH ROW
EXECUTE FUNCTION preencher_nome_profissional_legado();

CREATE OR REPLACE FUNCTION sincronizar_nome_usuario_legado()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.nome IS DISTINCT FROM OLD.nome AND NEW.nome IS NOT NULL THEN
        UPDATE profissional_saude
        SET nome = NEW.nome
        WHERE usuario_id = NEW.id;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sincronizar_nome_usuario_legado ON usuarios;

CREATE TRIGGER trg_sincronizar_nome_usuario_legado
AFTER UPDATE OF nome
ON usuarios
FOR EACH ROW
EXECUTE FUNCTION sincronizar_nome_usuario_legado();