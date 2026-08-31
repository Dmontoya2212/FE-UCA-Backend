DO $$
BEGIN
    ALTER TABLE usuarios ADD CONSTRAINT ck_usuarios_rol_valido
        CHECK (rol IN ('SUPERADMIN', 'ADMINISTRADOR', 'USUARIO')) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE iva_tasas ADD CONSTRAINT ck_iva_tasas_porcentaje
        CHECK (porcentaje >= 0 AND porcentaje <= 100) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE items ADD CONSTRAINT ck_items_precio_no_negativo
        CHECK (precio_sin_iva >= 0) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE facturas ADD CONSTRAINT ck_facturas_totales_no_negativos
        CHECK (subtotal_sin_iva >= 0 AND total_iva >= 0 AND total_con_iva >= 0) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE factura_lineas ADD CONSTRAINT ck_factura_lineas_valores_validos
        CHECK (
            cantidad > 0
            AND precio_sin_iva >= 0
            AND iva_porcentaje >= 0
            AND iva_porcentaje <= 100
            AND subtotal_sin_iva >= 0
            AND total_iva >= 0
            AND total_con_iva >= 0
        ) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE intentos_emision ADD CONSTRAINT ck_intentos_emision_numero_positivo
        CHECK (numero_intento > 0) NOT VALID;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
