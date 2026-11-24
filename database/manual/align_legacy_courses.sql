-- Executar com psql -v ON_ERROR_STOP=1 -f ...
-- Compatibiliza esquemas antigos sem excluir colunas, tabelas ou registros.
BEGIN;
SET LOCAL lock_timeout = '5s';

DO $$
DECLARE
    legacy_column text;
    legacy_fk record;
BEGIN
    IF to_regclass('public.courses') IS NULL THEN
        RAISE EXCEPTION 'Tabela public.courses não encontrada';
    END IF;

    FOREACH legacy_column IN ARRAY ARRAY['category', 'nome'] LOOP
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'courses'
              AND column_name = legacy_column
        ) THEN
            EXECUTE format('ALTER TABLE public.courses ALTER COLUMN %I DROP NOT NULL', legacy_column);
        END IF;
    END LOOP;

    -- Mantém a FK para categories; remove apenas a FK legada de category_id para category.
    FOR legacy_fk IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attname = 'category_id'
        WHERE c.contype = 'f'
          AND c.conrelid = 'public.courses'::regclass
          AND c.confrelid = to_regclass('public.category')
          AND c.conkey = ARRAY[a.attnum]::smallint[]
    LOOP
        EXECUTE format('ALTER TABLE public.courses DROP CONSTRAINT %I', legacy_fk.conname);
    END LOOP;
END $$;

COMMIT;
