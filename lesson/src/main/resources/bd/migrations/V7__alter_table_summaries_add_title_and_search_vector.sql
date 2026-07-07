ALTER TABLE summaries
  ADD COLUMN title varchar(200);

-- Backfill mínimo para linhas já existentes, caso a tabela já tenha dados
UPDATE summaries SET title = 'Sem título' WHERE title IS NULL;

ALTER TABLE summaries
  ALTER COLUMN title SET NOT NULL;

ALTER TABLE summaries
  ADD COLUMN search_vector tsvector
  GENERATED ALWAYS AS (
    to_tsvector('portuguese', coalesce(title, '') || ' ' || coalesce(content, ''))
  ) STORED;

CREATE INDEX idx_summaries_search_vector ON summaries USING GIN (search_vector);
