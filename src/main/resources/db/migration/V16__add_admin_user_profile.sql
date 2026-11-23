ALTER TABLE usuarios
    DROP CONSTRAINT IF EXISTS usuarios_tipo_perfil_check;

ALTER TABLE usuarios
    ADD CONSTRAINT usuarios_tipo_perfil_check
        CHECK (tipo_perfil IN ('PACIENTE', 'PROFISSIONAL_SAUDE', 'ADMIN'));
