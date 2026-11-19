ALTER TABLE pacientes
    DROP CONSTRAINT fk_pacientes_usuario,
    DROP CONSTRAINT uk_pacientes_usuario,
    DROP COLUMN usuario_id,
    ADD CONSTRAINT fk_pacientes_usuario FOREIGN KEY (id) REFERENCES usuarios (id);

ALTER TABLE profissional_saude
    DROP CONSTRAINT fk_profissional_saude_usuario,
    DROP CONSTRAINT uk_profissional_saude_usuario,
    DROP COLUMN usuario_id,
    ADD CONSTRAINT fk_profissional_saude_usuario FOREIGN KEY (id) REFERENCES usuarios (id);