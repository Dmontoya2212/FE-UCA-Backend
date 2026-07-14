DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invoice_status') THEN
        CREATE TYPE invoice_status AS ENUM (
            'BORRADOR',
            'LISTA_PARA_EMITIR',
            'ENVIANDO',
            'EMITIDA',
            'RECHAZADA',
            'CONTINGENCIA',
            'ANULADA',
            'PAGADA'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'item_category') THEN
        CREATE TYPE item_category AS ENUM ('SERVICIO', 'PRODUCTO', 'CONSULTORIA', 'OTRO');
    END IF;
END $$;

ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'BORRADOR';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'LISTA_PARA_EMITIR';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'ENVIANDO';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'EMITIDA';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'RECHAZADA';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'CONTINGENCIA';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'ANULADA';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'PAGADA';

CREATE TABLE IF NOT EXISTS empresas (
    id UUID PRIMARY KEY,
    razon_social VARCHAR(255),
    nombre_legal VARCHAR(255),
    nombre_comercial VARCHAR(255),
    nit VARCHAR(255),
    registro VARCHAR(255),
    actividad_economica VARCHAR(255),
    cod_actividad VARCHAR(6),
    sector_empresa VARCHAR(255),
    email VARCHAR(255),
    telefono VARCHAR(255),
    direccion VARCHAR(255),
    ciudad VARCHAR(255),
    codigo_postal VARCHAR(255),
    departamento VARCHAR(2),
    municipio VARCHAR(2),
    distrito VARCHAR(4),
    cod_establecimiento VARCHAR(4),
    cod_punto_venta VARCHAR(15),
    pais VARCHAR(255),
    usuario VARCHAR(255),
    password_hash TEXT,
    clave_primaria TEXT,
    token TEXT,
    expire_token VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS monedas (
    codigo VARCHAR(3) PRIMARY KEY,
    nombre VARCHAR(255),
    simbolo VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS empresa_monedas (
    empresa_id UUID NOT NULL,
    moneda_codigo VARCHAR(3) NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT pk_empresa_monedas PRIMARY KEY (empresa_id, moneda_codigo)
);

CREATE TABLE IF NOT EXISTS clientes (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    nombre_razon_social VARCHAR(255) NOT NULL,
    nif_cif VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    direccion VARCHAR(255),
    ciudad VARCHAR(255),
    codigo_postal VARCHAR(255),
    telefono VARCHAR(255),
    tipo_documento VARCHAR(2),
    nrc VARCHAR(8),
    cod_actividad VARCHAR(6),
    desc_actividad VARCHAR(150),
    departamento VARCHAR(2),
    municipio VARCHAR(2),
    distrito VARCHAR(4),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS iva_tasas (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    porcentaje NUMERIC(5, 2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS items (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    categoria item_category NOT NULL,
    iva_id UUID,
    iva_porcentaje_snapshot NUMERIC(5, 2),
    precio_sin_iva NUMERIC(18, 8) NOT NULL,
    codigo_interno VARCHAR(25),
    unidad_medida INTEGER DEFAULT 59,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS facturas (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    cliente_id UUID,
    numero VARCHAR(255) NOT NULL,
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE,
    estado invoice_status NOT NULL DEFAULT 'BORRADOR',
    moneda_codigo VARCHAR(3) NOT NULL DEFAULT 'USD',
    subtotal_sin_iva NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_iva NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_con_iva NUMERIC(19, 2) NOT NULL DEFAULT 0,
    cliente_nombre_razon_social VARCHAR(255),
    cliente_nif_cif VARCHAR(255),
    cliente_direccion VARCHAR(255),
    cliente_tipo_documento VARCHAR(2),
    cliente_nrc VARCHAR(8),
    cliente_cod_actividad VARCHAR(6),
    cliente_desc_actividad VARCHAR(150),
    cliente_departamento VARCHAR(2),
    cliente_municipio VARCHAR(2),
    cliente_distrito VARCHAR(4),
    cliente_telefono VARCHAR(255),
    cliente_email VARCHAR(255),
    emisor_nit VARCHAR(255),
    emisor_nrc VARCHAR(255),
    emisor_nombre VARCHAR(255),
    emisor_cod_actividad VARCHAR(6),
    emisor_desc_actividad VARCHAR(255),
    emisor_nombre_comercial VARCHAR(255),
    emisor_direccion VARCHAR(255),
    emisor_departamento VARCHAR(2),
    emisor_municipio VARCHAR(2),
    emisor_distrito VARCHAR(4),
    emisor_telefono VARCHAR(255),
    emisor_email VARCHAR(255),
    emisor_cod_establecimiento VARCHAR(255),
    emisor_cod_punto_venta VARCHAR(255),
    numero_control VARCHAR(31),
    codigo_generacion VARCHAR(36),
    condicion_operacion INTEGER DEFAULT 1,
    sello_recibido VARCHAR(255),
    fecha_recepcion TIMESTAMP WITH TIME ZONE,
    hacienda_codigo_respuesta VARCHAR(255),
    hacienda_mensaje_respuesta VARCHAR(1000),
    hacienda_errores VARCHAR(2000),
    hacienda_response_json TEXT,
    tipo_dte VARCHAR(2) DEFAULT '01',
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS factura_lineas (
    id UUID PRIMARY KEY,
    factura_id UUID NOT NULL,
    item_id UUID,
    descripcion VARCHAR(220) NOT NULL,
    item_codigo_interno VARCHAR(25),
    item_unidad_medida INTEGER,
    item_tipo INTEGER,
    item_categoria VARCHAR(50),
    cantidad NUMERIC(12, 2) NOT NULL,
    precio_sin_iva NUMERIC(18, 8) NOT NULL,
    iva_porcentaje NUMERIC(5, 2) NOT NULL,
    subtotal_sin_iva NUMERIC(18, 8) NOT NULL,
    total_iva NUMERIC(18, 8) NOT NULL,
    total_con_iva NUMERIC(18, 8) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS dte_secuencias (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    tipo_dte VARCHAR(2) NOT NULL,
    ultimo_correlativo BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    es_admin BOOLEAN NOT NULL DEFAULT false,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS usuario_empresas (
    usuario_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    CONSTRAINT pk_usuario_empresas PRIMARY KEY (usuario_id, empresa_id)
);

CREATE TABLE IF NOT EXISTS intentos_emision (
    id UUID PRIMARY KEY,
    factura_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    codigo_generacion VARCHAR(36),
    numero_control VARCHAR(31),
    ambiente VARCHAR(2),
    idempotency_key VARCHAR(120),
    estado_intento VARCHAR(50) NOT NULL,
    codigo_http INTEGER,
    codigo_hacienda VARCHAR(255),
    descripcion_respuesta VARCHAR(1000),
    sello_recibido VARCHAR(255),
    request_json TEXT,
    response_json TEXT,
    mensaje VARCHAR(1000),
    numero_intento INTEGER NOT NULL DEFAULT 1,
    error_tecnico VARCHAR(2000),
    fecha_intento TIMESTAMP WITH TIME ZONE,
    fecha_respuesta TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);
