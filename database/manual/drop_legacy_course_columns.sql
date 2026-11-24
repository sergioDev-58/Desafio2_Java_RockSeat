-- Executar após align_legacy_courses.sql em instalações com esquema antigo.
-- Remove somente colunas substituídas ou não utilizadas pelo cadastro atual.
BEGIN;
SET LOCAL lock_timeout = '5s';
ALTER TABLE public.courses
    DROP COLUMN IF EXISTS category,
    DROP COLUMN IF EXISTS nome;
COMMIT;
