ALTER TABLE empresa_monedas ADD COLUMN IF NOT EXISTS principal BOOLEAN DEFAULT false;

UPDATE empresa_monedas SET principal = false WHERE principal IS NULL;

ALTER TABLE empresa_monedas ALTER COLUMN principal SET DEFAULT false;
ALTER TABLE empresa_monedas ALTER COLUMN principal SET NOT NULL;
