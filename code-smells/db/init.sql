-- Code Smells course schema.
--
-- The labs are self-contained: each refactor check stores one row per attempt
-- so the /scoreboard sees real, persistent evidence of every identify/refactor
-- verdict the learner produces.

CREATE TABLE IF NOT EXISTS smell_attempts (
    id               bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lab              text        NOT NULL,
    player           text        NOT NULL DEFAULT 'anonymous',
    identify_pick    text        NOT NULL,
    refactor_pick    text        NOT NULL,
    identify_correct boolean     NOT NULL,
    refactor_correct boolean     NOT NULL,
    created_at       timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS smell_attempts_lab_idx   ON smell_attempts (lab);
CREATE INDEX IF NOT EXISTS smell_attempts_player_idx ON smell_attempts (player);

-- The app-login role (created idempotently; safe to re-run on seeds).
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'smells_app') THEN
      CREATE ROLE smells_app LOGIN PASSWORD 'smells_app';
   END IF;
END
$$;

GRANT SELECT, INSERT, UPDATE, DELETE ON smell_attempts TO smells_app;
GRANT USAGE, SELECT ON SEQUENCE smell_attempts_id_seq TO smells_app;