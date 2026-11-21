-- =============================================================================
-- Nomes únicos para palavras e categorias, sem diferenciar maiúsculas e minúsculas
-- nem considerar espaços nas pontas ("uva", " Uva " e "Uva" são o mesmo nome).
--
-- O serviço já recusa nomes repetidos com 409; os índices abaixo garantem a regra
-- também no banco. Antes de criá-los, as duplicatas que já existirem (por exemplo,
-- criadas antes desta versão) são unidas: fica o registro mais antigo (menor id),
-- que herda as categorias / palavras e as respostas de quiz dos repetidos.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Palavras
-- ---------------------------------------------------------------------------

-- Pares (repetida -> mantida).
CREATE TEMPORARY TABLE palavras_repetidas AS
SELECT id, manter
FROM (SELECT id, min(id) OVER (PARTITION BY lower(trim(palavra))) AS manter FROM palavras) p
WHERE id <> manter;

-- A palavra mantida passa a ter também as categorias das repetidas.
INSERT INTO palavras_categorias (palavra_id, categoria_id)
SELECT r.manter, pc.categoria_id
FROM palavras_categorias pc
JOIN palavras_repetidas r ON r.id = pc.palavra_id
ON CONFLICT DO NOTHING;

-- As respostas de quiz apontam para a palavra mantida (o FK é RESTRICT).
UPDATE quiz_palavras qp
SET id_palavra = r.manter
FROM palavras_repetidas r
WHERE qp.id_palavra = r.id;

-- Os vínculos das repetidas saem em cascata.
DELETE FROM palavras p
USING palavras_repetidas r
WHERE p.id = r.id;

UPDATE palavras SET palavra = trim(palavra) WHERE palavra <> trim(palavra);

CREATE UNIQUE INDEX uk_palavras_palavra ON palavras (lower(trim(palavra)));

-- ---------------------------------------------------------------------------
-- Categorias
-- ---------------------------------------------------------------------------

CREATE TEMPORARY TABLE categorias_repetidas AS
SELECT id, manter
FROM (SELECT id, min(id) OVER (PARTITION BY lower(trim(nome))) AS manter FROM categorias) c
WHERE id <> manter;

-- A categoria mantida recebe as palavras das repetidas, para nenhuma palavra ficar sem categoria.
INSERT INTO palavras_categorias (palavra_id, categoria_id)
SELECT pc.palavra_id, r.manter
FROM palavras_categorias pc
JOIN categorias_repetidas r ON r.id = pc.categoria_id
ON CONFLICT DO NOTHING;

DELETE FROM categorias c
USING categorias_repetidas r
WHERE c.id = r.id;

UPDATE categorias SET nome = trim(nome) WHERE nome <> trim(nome);

CREATE UNIQUE INDEX uk_categorias_nome ON categorias (lower(trim(nome)));

DROP TABLE palavras_repetidas, categorias_repetidas;
