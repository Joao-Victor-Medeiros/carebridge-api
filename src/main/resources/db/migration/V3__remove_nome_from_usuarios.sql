DROP TRIGGER IF EXISTS trg_sincronizar_nome_usuario_legado
ON usuarios;

DROP TRIGGER IF EXISTS trg_preencher_nome_profissional_legado
ON profissional_saude;

DROP FUNCTION IF EXISTS sincronizar_nome_usuario_legado();

DROP FUNCTION IF EXISTS preencher_nome_profissional_legado();

ALTER TABLE usuarios
DROP COLUMN IF EXISTS nome;