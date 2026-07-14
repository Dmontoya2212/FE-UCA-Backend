CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    usuario_id UUID,
    usuario_email VARCHAR(255),
    empresa_id UUID,
    accion VARCHAR(120) NOT NULL,
    recurso VARCHAR(120) NOT NULL,
    recurso_id VARCHAR(120),
    resultado VARCHAR(40) NOT NULL,
    ip VARCHAR(80),
    user_agent VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_usuario ON audit_logs (usuario_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_empresa ON audit_logs (empresa_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_recurso ON audit_logs (recurso, recurso_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_accion_resultado ON audit_logs (accion, resultado);

DO $$
BEGIN
    ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
