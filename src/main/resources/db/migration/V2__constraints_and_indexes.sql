UPDATE usuarios SET email = lower(trim(email)) WHERE email IS NOT NULL;
UPDATE clientes SET email = lower(trim(email)) WHERE email IS NOT NULL;
UPDATE iva_tasas SET activo = true WHERE activo IS NULL;
UPDATE items SET unidad_medida = 59 WHERE unidad_medida IS NULL;
UPDATE items SET activo = true WHERE activo IS NULL;
UPDATE facturas SET subtotal_sin_iva = 0 WHERE subtotal_sin_iva IS NULL;
UPDATE facturas SET total_iva = 0 WHERE total_iva IS NULL;
UPDATE facturas SET total_con_iva = 0 WHERE total_con_iva IS NULL;
UPDATE facturas SET version = 0 WHERE version IS NULL;
UPDATE empresa_monedas SET principal = false WHERE principal IS NULL;

WITH primeras AS (
    SELECT empresa_id, MIN(moneda_codigo) AS moneda_codigo
    FROM empresa_monedas
    GROUP BY empresa_id
)
UPDATE empresa_monedas em
SET principal = true
FROM primeras p
WHERE em.empresa_id = p.empresa_id
  AND em.moneda_codigo = p.moneda_codigo
  AND NOT EXISTS (
      SELECT 1
      FROM empresa_monedas x
      WHERE x.empresa_id = em.empresa_id
        AND x.principal = true
  );

ALTER TABLE clientes DROP CONSTRAINT IF EXISTS unique_nif_cif;

CREATE UNIQUE INDEX IF NOT EXISTS ux_empresas_nit ON empresas (nit) WHERE nit IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_empresas_email ON empresas (lower(email)) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_empresas_telefono ON empresas (telefono) WHERE telefono IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_email_normalizado ON usuarios (lower(email));
CREATE UNIQUE INDEX IF NOT EXISTS ux_clientes_empresa_nif_cif ON clientes (empresa_id, lower(nif_cif)) WHERE deleted_at IS NULL AND nif_cif IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_clientes_empresa_email ON clientes (empresa_id, lower(email)) WHERE deleted_at IS NULL AND email IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_items_empresa_nombre ON items (empresa_id, lower(nombre)) WHERE deleted_at IS NULL AND nombre IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_items_empresa_codigo_interno ON items (empresa_id, lower(codigo_interno)) WHERE deleted_at IS NULL AND codigo_interno IS NOT NULL;
DROP INDEX IF EXISTS ux_iva_tasas_empresa_nombre;
DROP INDEX IF EXISTS ux_iva_tasas_empresa_porcentaje;
CREATE UNIQUE INDEX IF NOT EXISTS ux_iva_tasas_empresa_nombre_activa ON iva_tasas (empresa_id, lower(nombre)) WHERE deleted_at IS NULL AND nombre IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_iva_tasas_empresa_porcentaje_activa ON iva_tasas (empresa_id, porcentaje) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_dte_secuencias_empresa_tipo ON dte_secuencias (empresa_id, tipo_dte);
CREATE UNIQUE INDEX IF NOT EXISTS ux_facturas_empresa_numero ON facturas (empresa_id, numero);
CREATE UNIQUE INDEX IF NOT EXISTS ux_facturas_codigo_generacion ON facturas (codigo_generacion) WHERE codigo_generacion IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_facturas_numero_control ON facturas (numero_control) WHERE numero_control IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_intentos_emision_idempotency ON intentos_emision (factura_id, idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_empresa_monedas_principal ON empresa_monedas (empresa_id) WHERE principal = true;

CREATE INDEX IF NOT EXISTS idx_clientes_empresa ON clientes (empresa_id);
CREATE INDEX IF NOT EXISTS idx_clientes_empresa_nombre ON clientes (empresa_id, lower(nombre_razon_social));
CREATE INDEX IF NOT EXISTS idx_items_empresa ON items (empresa_id);
CREATE INDEX IF NOT EXISTS idx_items_iva ON items (iva_id);
CREATE INDEX IF NOT EXISTS idx_iva_tasas_empresa ON iva_tasas (empresa_id);
CREATE INDEX IF NOT EXISTS idx_facturas_empresa ON facturas (empresa_id);
CREATE INDEX IF NOT EXISTS idx_facturas_cliente ON facturas (cliente_id);
CREATE INDEX IF NOT EXISTS idx_factura_lineas_factura ON factura_lineas (factura_id);
CREATE INDEX IF NOT EXISTS idx_factura_lineas_item ON factura_lineas (item_id);
CREATE INDEX IF NOT EXISTS idx_intentos_emision_factura ON intentos_emision (factura_id);
CREATE INDEX IF NOT EXISTS idx_usuario_empresas_empresa ON usuario_empresas (empresa_id);
CREATE INDEX IF NOT EXISTS idx_empresa_monedas_moneda ON empresa_monedas (moneda_codigo);

ALTER TABLE empresa_monedas ALTER COLUMN principal SET DEFAULT false;
ALTER TABLE empresa_monedas ALTER COLUMN principal SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN nombre_razon_social SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN nif_cif SET NOT NULL;
ALTER TABLE clientes ALTER COLUMN activo SET DEFAULT true;
ALTER TABLE clientes ALTER COLUMN activo SET NOT NULL;
ALTER TABLE iva_tasas ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE iva_tasas ALTER COLUMN nombre SET NOT NULL;
ALTER TABLE iva_tasas ALTER COLUMN porcentaje SET NOT NULL;
ALTER TABLE iva_tasas ALTER COLUMN activo SET DEFAULT true;
ALTER TABLE iva_tasas ALTER COLUMN activo SET NOT NULL;
ALTER TABLE items ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE items ALTER COLUMN nombre SET NOT NULL;
ALTER TABLE items ALTER COLUMN categoria SET NOT NULL;
ALTER TABLE items ALTER COLUMN precio_sin_iva SET NOT NULL;
ALTER TABLE items ALTER COLUMN unidad_medida SET DEFAULT 59;
ALTER TABLE items ALTER COLUMN activo SET DEFAULT true;
ALTER TABLE items ALTER COLUMN activo SET NOT NULL;
ALTER TABLE facturas ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE facturas ALTER COLUMN numero SET NOT NULL;
ALTER TABLE facturas ALTER COLUMN fecha_emision SET NOT NULL;
ALTER TABLE facturas ALTER COLUMN estado SET NOT NULL;
ALTER TABLE facturas ALTER COLUMN moneda_codigo SET NOT NULL;
ALTER TABLE facturas ALTER COLUMN subtotal_sin_iva SET NOT NULL;
ALTER TABLE facturas ALTER COLUMN total_iva SET NOT NULL;
ALTER TABLE facturas ALTER COLUMN total_con_iva SET NOT NULL;
ALTER TABLE factura_lineas ALTER COLUMN factura_id SET NOT NULL;
ALTER TABLE factura_lineas ALTER COLUMN descripcion SET NOT NULL;
ALTER TABLE factura_lineas ALTER COLUMN cantidad SET NOT NULL;
ALTER TABLE factura_lineas ALTER COLUMN precio_sin_iva SET NOT NULL;
ALTER TABLE factura_lineas ALTER COLUMN iva_porcentaje SET NOT NULL;
ALTER TABLE factura_lineas ALTER COLUMN subtotal_sin_iva SET NOT NULL;
ALTER TABLE factura_lineas ALTER COLUMN total_iva SET NOT NULL;
ALTER TABLE factura_lineas ALTER COLUMN total_con_iva SET NOT NULL;

DO $$
BEGIN
    ALTER TABLE clientes ADD CONSTRAINT fk_clientes_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE iva_tasas ADD CONSTRAINT fk_iva_tasas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE items ADD CONSTRAINT fk_items_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE items ADD CONSTRAINT fk_items_iva FOREIGN KEY (iva_id) REFERENCES iva_tasas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE facturas ADD CONSTRAINT fk_facturas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE facturas ADD CONSTRAINT fk_facturas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE facturas ADD CONSTRAINT fk_facturas_moneda FOREIGN KEY (moneda_codigo) REFERENCES monedas(codigo) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE factura_lineas ADD CONSTRAINT fk_factura_lineas_factura FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE CASCADE NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE factura_lineas ADD CONSTRAINT fk_factura_lineas_item FOREIGN KEY (item_id) REFERENCES items(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE dte_secuencias ADD CONSTRAINT fk_dte_secuencias_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE intentos_emision ADD CONSTRAINT fk_intentos_emision_factura FOREIGN KEY (factura_id) REFERENCES facturas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE intentos_emision ADD CONSTRAINT fk_intentos_emision_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE usuario_empresas ADD CONSTRAINT fk_usuario_empresas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE usuario_empresas ADD CONSTRAINT fk_usuario_empresas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE empresa_monedas ADD CONSTRAINT fk_empresa_monedas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE empresa_monedas ADD CONSTRAINT fk_empresa_monedas_moneda FOREIGN KEY (moneda_codigo) REFERENCES monedas(codigo) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
