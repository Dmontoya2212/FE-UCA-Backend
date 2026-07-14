package com.feuca.facturacion;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.database-initializer", name = "enabled", havingValue = "true")
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==================================================");
        System.out.println("--- RUNNING MANUAL DATABASE MIGRATION ---");
        System.out.println("==================================================");
        
        try {
            // =========================================================
            // BLOQUE 1: MIGRACIONES DE ESTRUCTURA (CREATE / ALTER TABLE)
            // =========================================================
            System.out.println("1. Ejecutando migraciones de estructura...");
            
            // 1.1 Migraciones de Empresas
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS razon_social VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS nombre_legal VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS nombre_comercial VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS nit VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS registro VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS actividad_economica VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS sector_empresa VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS email VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS telefono VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS direccion VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS ciudad VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS codigo_postal VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS pais VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS usuario VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS password_hash TEXT");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS clave_primaria TEXT");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS token TEXT");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS expire_token VARCHAR(255)");
            
            // DTE Empresas
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS cod_actividad VARCHAR(6)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS departamento VARCHAR(2)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS municipio VARCHAR(2)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS distrito VARCHAR(4)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS cod_establecimiento VARCHAR(4)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS cod_punto_venta VARCHAR(15)");
            
            // Constraints de Empresas
            try { jdbcTemplate.execute("ALTER TABLE empresas ADD CONSTRAINT unique_nit UNIQUE (nit)"); } catch(Exception e) {}
            try { jdbcTemplate.execute("ALTER TABLE empresas ADD CONSTRAINT unique_email UNIQUE (email)"); } catch(Exception e) {}
            try { jdbcTemplate.execute("ALTER TABLE empresas ADD CONSTRAINT unique_telefono UNIQUE (telefono)"); } catch(Exception e) {}

            // 1.2 Migraciones de Clientes
            // DTE Clientes
            jdbcTemplate.execute("ALTER TABLE clientes ADD COLUMN IF NOT EXISTS tipo_documento VARCHAR(2)");
            jdbcTemplate.execute("ALTER TABLE clientes ADD COLUMN IF NOT EXISTS nrc VARCHAR(8)");
            jdbcTemplate.execute("ALTER TABLE clientes ADD COLUMN IF NOT EXISTS cod_actividad VARCHAR(6)");
            jdbcTemplate.execute("ALTER TABLE clientes ADD COLUMN IF NOT EXISTS desc_actividad VARCHAR(150)");
            jdbcTemplate.execute("ALTER TABLE clientes ADD COLUMN IF NOT EXISTS departamento VARCHAR(2)");
            jdbcTemplate.execute("ALTER TABLE clientes ADD COLUMN IF NOT EXISTS municipio VARCHAR(2)");
            jdbcTemplate.execute("ALTER TABLE clientes ADD COLUMN IF NOT EXISTS distrito VARCHAR(4)");
            
            try { jdbcTemplate.execute("ALTER TABLE clientes ADD CONSTRAINT unique_nif_cif UNIQUE (nif_cif)"); } catch (Exception e) {}

            // 1.3 Migraciones de Items
            // DTE Items
            jdbcTemplate.execute("ALTER TABLE items ADD COLUMN IF NOT EXISTS codigo_interno VARCHAR(25)");
            jdbcTemplate.execute("ALTER TABLE items ADD COLUMN IF NOT EXISTS unidad_medida INTEGER DEFAULT 59");

            // 1.3.1 Migraciones de IVA
            jdbcTemplate.execute("ALTER TABLE iva_tasas ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT true");
            jdbcTemplate.execute("ALTER TABLE iva_tasas ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE");
            jdbcTemplate.execute("UPDATE iva_tasas SET activo = true WHERE activo IS NULL");

            // 1.4 Migraciones de Facturas y Lineas
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS facturas (" +
                    "id UUID PRIMARY KEY," +
                    "empresa_id UUID NOT NULL," +
                    "cliente_id UUID," +
                    "numero VARCHAR(255) NOT NULL," +
                    "fecha_emision DATE NOT NULL," +
                    "estado VARCHAR(50) NOT NULL," +
                    "moneda_codigo VARCHAR(10)," +
                    "subtotal_sin_iva NUMERIC(19, 2)," +
                    "total_iva NUMERIC(19, 2)," +
                    "total_con_iva NUMERIC(19, 2)," +
                    "created_at TIMESTAMP WITH TIME ZONE," +
                    "updated_at TIMESTAMP WITH TIME ZONE" +
                    ")");
            } catch (Exception e) {
                System.out.println("No se pudo crear tabla facturas: " + e.getMessage());
            }

            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS factura_lineas (" +
                    "id UUID PRIMARY KEY," +
                    "factura_id UUID NOT NULL," +
                    "item_id UUID," +
                    "descripcion VARCHAR(220) NOT NULL," +
                    "cantidad NUMERIC(12, 2) NOT NULL," +
                    "precio_sin_iva NUMERIC(18, 8) NOT NULL," +
                    "iva_porcentaje NUMERIC(5, 2) NOT NULL," +
                    "subtotal_sin_iva NUMERIC(18, 8) NOT NULL," +
                    "total_iva NUMERIC(18, 8) NOT NULL," +
                    "total_con_iva NUMERIC(18, 8) NOT NULL," +
                    "created_at TIMESTAMP WITH TIME ZONE," +
                    "updated_at TIMESTAMP WITH TIME ZONE" +
                    ")");
            } catch (Exception e) {
                System.out.println("No se pudo crear tabla factura_lineas: " + e.getMessage());
            }

            try {
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS estado VARCHAR(50)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS fecha_vencimiento DATE");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS moneda_codigo VARCHAR(10)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS subtotal_sin_iva NUMERIC(19, 2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS total_iva NUMERIC(19, 2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS total_con_iva NUMERIC(19, 2)");
                
                // Cliente denormalization
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_nombre_razon_social VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_nif_cif VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_direccion VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_tipo_documento VARCHAR(2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_nrc VARCHAR(8)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_cod_actividad VARCHAR(6)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_desc_actividad VARCHAR(150)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_departamento VARCHAR(2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_municipio VARCHAR(2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_distrito VARCHAR(4)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_telefono VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_email VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_nit VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_nrc VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_nombre VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_cod_actividad VARCHAR(6)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_desc_actividad VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_nombre_comercial VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_direccion VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_departamento VARCHAR(2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_municipio VARCHAR(2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_distrito VARCHAR(4)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_telefono VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_email VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_cod_establecimiento VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS emisor_cod_punto_venta VARCHAR(255)");
                
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE");

                // DTE Facturas
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS numero_control VARCHAR(31)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS codigo_generacion VARCHAR(36)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS condicion_operacion INTEGER DEFAULT 1");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS sello_recibido VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS fecha_recepcion TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS hacienda_codigo_respuesta VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS hacienda_mensaje_respuesta VARCHAR(1000)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS hacienda_errores VARCHAR(2000)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS hacienda_response_json TEXT");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS tipo_dte VARCHAR(2) DEFAULT '01'");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0");
                jdbcTemplate.execute("UPDATE facturas SET version = 0 WHERE version IS NULL");

                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS subtotal_sin_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS total_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS total_con_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS item_codigo_interno VARCHAR(25)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS item_unidad_medida INTEGER");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS item_tipo INTEGER");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS item_categoria VARCHAR(50)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE");
            } catch (Exception e) {
                System.out.println("Error al alterar columnas de facturas/factura_lineas: " + e.getMessage());
            }
            migrateInvoiceStatusEnum();

            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS intentos_emision (" +
                    "id UUID PRIMARY KEY," +
                    "factura_id UUID NOT NULL," +
                    "empresa_id UUID NOT NULL," +
                    "codigo_generacion VARCHAR(36)," +
                    "numero_control VARCHAR(31)," +
                    "ambiente VARCHAR(2)," +
                    "idempotency_key VARCHAR(120)," +
                    "estado_intento VARCHAR(50) NOT NULL," +
                    "codigo_http INTEGER," +
                    "codigo_hacienda VARCHAR(255)," +
                    "descripcion_respuesta VARCHAR(1000)," +
                    "sello_recibido VARCHAR(255)," +
                    "request_json TEXT," +
                    "response_json TEXT," +
                    "mensaje VARCHAR(1000)," +
                    "numero_intento INTEGER NOT NULL DEFAULT 1," +
                    "error_tecnico VARCHAR(2000)," +
                    "fecha_intento TIMESTAMP WITH TIME ZONE," +
                    "fecha_respuesta TIMESTAMP WITH TIME ZONE," +
                    "created_at TIMESTAMP WITH TIME ZONE," +
                    "updated_at TIMESTAMP WITH TIME ZONE" +
                    ")");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS numero_control VARCHAR(31)");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS ambiente VARCHAR(2)");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS codigo_http INTEGER");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS codigo_hacienda VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS descripcion_respuesta VARCHAR(1000)");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS sello_recibido VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS request_json TEXT");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS numero_intento INTEGER DEFAULT 1");
                jdbcTemplate.execute("UPDATE intentos_emision SET numero_intento = 1 WHERE numero_intento IS NULL");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ALTER COLUMN numero_intento SET NOT NULL");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS error_tecnico VARCHAR(2000)");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS fecha_intento TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE intentos_emision ADD COLUMN IF NOT EXISTS fecha_respuesta TIMESTAMP WITH TIME ZONE");
            } catch (Exception e) {
                System.out.println("Error creando intentos_emision: " + e.getMessage());
            }

            // 1.5 Migraciones de DTE Secuencias
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dte_secuencias (" +
                    "id UUID PRIMARY KEY," +
                    "empresa_id UUID NOT NULL," +
                    "tipo_dte VARCHAR(2) NOT NULL," +
                    "ultimo_correlativo BIGINT NOT NULL DEFAULT 0," +
                    "UNIQUE(empresa_id, tipo_dte)" +
                    ")");
            } catch (Exception e) {
                System.out.println("Error creando dte_secuencias: " + e.getMessage());
            }

            // 1.6 Migraciones de Usuarios
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id UUID PRIMARY KEY," +
                    "nombre VARCHAR(255) NOT NULL," +
                    "email VARCHAR(255) NOT NULL UNIQUE," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "es_admin BOOLEAN NOT NULL," +
                    "activo BOOLEAN NOT NULL," +
                    "created_at TIMESTAMP WITH TIME ZONE," +
                    "updated_at TIMESTAMP WITH TIME ZONE" +
                    ")");
            } catch (Exception e) {
                System.out.println("No se pudo crear tabla usuarios: " + e.getMessage());
            }

            // 1.7 Migraciones de Roles
            jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS rol VARCHAR(20)");
            // Set default rol for existing users that don't have one
            jdbcTemplate.execute("UPDATE usuarios SET rol = 'ADMINISTRADOR' WHERE rol IS NULL AND es_admin = true");
            jdbcTemplate.execute("UPDATE usuarios SET rol = 'USUARIO' WHERE rol IS NULL AND es_admin = false");
            // admin@selectos.com is SUPERADMIN
            jdbcTemplate.execute("UPDATE usuarios SET rol = 'SUPERADMIN' WHERE email = 'admin@selectos.com'");
            jdbcTemplate.execute("UPDATE usuarios SET es_admin = CASE WHEN rol IN ('SUPERADMIN', 'ADMINISTRADOR') THEN true ELSE false END");
            jdbcTemplate.execute("ALTER TABLE usuarios ALTER COLUMN rol SET NOT NULL");

            System.out.println("Migraciones de estructura completadas con éxito.");

            // 1.8 Migraciones de Relación N:N (usuario_empresas)
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS usuario_empresas (" +
                    "usuario_id UUID NOT NULL," +
                    "empresa_id UUID NOT NULL," +
                    "PRIMARY KEY(usuario_id, empresa_id)," +
                    "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE," +
                    "FOREIGN KEY(empresa_id) REFERENCES empresas(id) ON DELETE CASCADE" +
                    ")");
                
                // Migrate existing relationships if empresa_id exists
                try {
                    jdbcTemplate.execute("INSERT INTO usuario_empresas (usuario_id, empresa_id) " +
                        "SELECT id, empresa_id FROM usuarios " +
                        "ON CONFLICT DO NOTHING");
                    // Drop the old column
                    jdbcTemplate.execute("ALTER TABLE usuarios DROP COLUMN IF EXISTS empresa_id CASCADE");
                } catch (Exception e) {
                    System.out.println("Columna empresa_id ya fue migrada/eliminada: " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("No se pudo crear tabla usuario_empresas: " + e.getMessage());
            }

            hardenDataIntegrityConstraints();

            // =========================================================
            // BLOQUE 2: SEEDING (INSERT DE DATOS)
            // =========================================================
            System.out.println("2. Ejecutando seeding de base de datos...");
            
            // 2.1 Empresa Seeding
            System.out.println("Sembrando empresas de prueba...");
            jdbcTemplate.update(
                "INSERT INTO empresas (id, razon_social, nombre_legal, nombre_comercial, nit, email, telefono, direccion, actividad_economica, ciudad, pais, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) ON CONFLICT (nit) DO NOTHING",
                UUID.fromString("2a1a1661-d7a8-4e89-8d1b-85cf7510d9fa"),
                "Corporación de Alimentos S.A. de C.V.",
                "Corporación de Alimentos S.A. de C.V.",
                "Super Selectos Escalón",
                "0614-120392-101-4",
                "contacto@selectos.com.sv",
                "+503 2264-8500",
                "Paseo General Escalón y 75 Av. Norte, San Salvador",
                "Comercio de productos de primera necesidad",
                "San Salvador",
                "El Salvador"
            );

            jdbcTemplate.update(
                "INSERT INTO empresas (id, razon_social, nombre_legal, nombre_comercial, nit, email, telefono, direccion, actividad_economica, ciudad, pais, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) ON CONFLICT (nit) DO NOTHING",
                UUID.fromString("3b2b2772-e8b9-5f9a-9e2c-96df8621e0fb"),
                "Telecomunicaciones del Centro S.A. de C.V.",
                "Telecomunicaciones del Centro S.A. de C.V.",
                "Tigo El Salvador",
                "0614-250888-102-5",
                "soporte@tigo.com.sv",
                "+503 2508-0000",
                "Carretera a La Libertad, Km 10, Santa Tecla",
                "Servicios de telecomunicaciones",
                "Santa Tecla",
                "El Salvador"
            );

            jdbcTemplate.update(
                "INSERT INTO empresas (id, razon_social, nombre_legal, nombre_comercial, nit, email, telefono, direccion, actividad_economica, ciudad, pais, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) ON CONFLICT (nit) DO NOTHING",
                UUID.fromString("4c3c3883-f9c0-6a0b-af3d-a7ef9732f1fc"),
                "Desarrollos Inmobiliarios Escalón S.A.",
                "Desarrollos Inmobiliarios Escalón S.A.",
                "Torre Futura Offices",
                "0614-051185-103-9",
                "arrendamiento@futura.com.sv",
                "+503 2211-6000",
                "Calle El Mirador y 87 Av. Norte, Colonia Escalón, San Salvador",
                "Arrendamiento de bienes inmuebles",
                "San Salvador",
                "El Salvador"
            );

            // Get first company id
            UUID firstEmpresaId = null;
            try {
                firstEmpresaId = jdbcTemplate.queryForObject("SELECT id FROM empresas LIMIT 1", UUID.class);
            } catch (Exception e) {
                System.out.println("No se pudo obtener empresaId para seeding: " + e.getMessage());
            }

            if (firstEmpresaId != null) {
                // 2.2 Clientes Seeding
                System.out.println("Sembrando clientes de prueba...");
                jdbcTemplate.update(
                    "INSERT INTO clientes (id, empresa_id, nombre_razon_social, nif_cif, email, direccion, ciudad, codigo_postal, telefono, activo, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, NOW(), NOW()) ON CONFLICT DO NOTHING",
                    UUID.randomUUID(),
                    firstEmpresaId,
                    "Industrias La Constancia S.A. de C.V.",
                    "0614-250912-101-1",
                    "compras@ilc.com.sv",
                    "Paseo General Escalón, No. 3700, San Salvador",
                    "San Salvador",
                    "01101",
                    "+503 2222-3000"
                );

                jdbcTemplate.update(
                    "INSERT INTO clientes (id, empresa_id, nombre_razon_social, nif_cif, email, direccion, ciudad, codigo_postal, telefono, activo, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, NOW(), NOW()) ON CONFLICT DO NOTHING",
                    UUID.randomUUID(),
                    firstEmpresaId,
                    "Droguería Santa Lucía S.A.",
                    "0614-040280-102-3",
                    "info@drogueriasantalucia.com",
                    "29 Calle Oriente y 10 Av. Norte, San Salvador",
                    "San Salvador",
                    "01101",
                    "+503 2239-1000"
                );

                jdbcTemplate.update(
                    "INSERT INTO clientes (id, empresa_id, nombre_razon_social, nif_cif, email, direccion, ciudad, codigo_postal, telefono, activo, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, NOW(), NOW()) ON CONFLICT DO NOTHING",
                    UUID.randomUUID(),
                    firstEmpresaId,
                    "Constructora El Sol S.A. de C.V.",
                    "0614-111195-103-5",
                    "proyectos@elsol.com.sv",
                    "Bulevar Orden de Malta, Santa Elena, Antiguo Cuscatlán",
                    "Antiguo Cuscatlán",
                    "01104",
                    "+503 2555-4400"
                );

                // 2.3 Items Seeding
                System.out.println("Sembrando items de prueba...");
                try {
                    UUID ivaTasaId = UUID.randomUUID();
                    try {
                        ivaTasaId = jdbcTemplate.queryForObject(
                            "SELECT id FROM iva_tasas WHERE empresa_id = ? AND porcentaje = 13.00 LIMIT 1",
                            UUID.class,
                            firstEmpresaId
                        );
                    } catch (Exception e) {
                        jdbcTemplate.update(
                            "INSERT INTO iva_tasas (id, empresa_id, nombre, porcentaje, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())",
                            ivaTasaId,
                            firstEmpresaId,
                            "IVA Estándar (13%)",
                            new java.math.BigDecimal("13.00")
                        );
                    }

                    Integer itemsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items WHERE empresa_id = ?", Integer.class, firstEmpresaId);
                    if (itemsCount != null && itemsCount == 0) {
                        jdbcTemplate.update(
                            "INSERT INTO items (id, empresa_id, nombre, descripcion, categoria, iva_id, iva_porcentaje_snapshot, precio_sin_iva, activo, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?::item_category, ?, ?, ?, true, NOW(), NOW())",
                            UUID.randomUUID(),
                            firstEmpresaId,
                            "Desarrollo de Software Integrado",
                            "Desarrollo a medida de sistemas web y aplicaciones empresariales",
                            "SERVICIO",
                            ivaTasaId,
                            new java.math.BigDecimal("13.00"),
                            new java.math.BigDecimal("1200.00000000")
                        );

                        jdbcTemplate.update(
                            "INSERT INTO items (id, empresa_id, nombre, descripcion, categoria, iva_id, iva_porcentaje_snapshot, precio_sin_iva, activo, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?::item_category, ?, ?, ?, true, NOW(), NOW())",
                            UUID.randomUUID(),
                            firstEmpresaId,
                            "Licencia Corporativa Anual ERP",
                            "Licencia de uso anual para plataforma de facturación y ERP en la nube",
                            "PRODUCTO",
                            ivaTasaId,
                            new java.math.BigDecimal("13.00"),
                            new java.math.BigDecimal("450.00000000")
                        );

                        jdbcTemplate.update(
                            "INSERT INTO items (id, empresa_id, nombre, descripcion, categoria, iva_id, iva_porcentaje_snapshot, precio_sin_iva, activo, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?::item_category, ?, ?, ?, true, NOW(), NOW())",
                            UUID.randomUUID(),
                            firstEmpresaId,
                            "Consultoría Técnica en Facturación Electrónica",
                            "Asesoría técnica para la integración y habilitación ante el Ministerio de Hacienda",
                            "CONSULTORIA",
                            ivaTasaId,
                            new java.math.BigDecimal("13.00"),
                            new java.math.BigDecimal("85.00000000")
                        );
                        System.out.println("Items sembrados exitosamente.");
                    } else {
                        System.out.println("Los items ya estaban sembrados.");
                    }
                } catch (Exception e) {
                    System.out.println("Error sembrando items: " + e.getMessage());
                }

                // 2.4 Facturas Seeding
                Integer facturasCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM facturas WHERE numero = 'F-000001'", Integer.class);
                if (facturasCount != null && facturasCount == 0) {
                    System.out.println("Sembrando facturas de prueba...");
                    
                    UUID clienteId = null;
                    String clienteNombre = "Industrias La Constancia S.A. de C.V.";
                    String clienteNif = "0614-250912-101-1";
                    String clienteDir = "Paseo General Escalón, San Salvador";
                    try {
                        java.util.List<java.util.Map<String, Object>> clientsList = jdbcTemplate.queryForList(
                            "SELECT id, nombre_razon_social, nif_cif, direccion FROM clientes WHERE empresa_id = ? LIMIT 1",
                            firstEmpresaId
                        );
                        if (!clientsList.isEmpty()) {
                            java.util.Map<String, Object> cliMap = clientsList.get(0);
                            clienteId = (UUID) cliMap.get("id");
                            clienteNombre = (String) cliMap.get("nombre_razon_social");
                            clienteNif = (String) cliMap.get("nif_cif");
                            clienteDir = (String) cliMap.get("direccion");
                        }
                    } catch (Exception e) {
                        System.out.println("No se pudo obtener cliente para facturas: " + e.getMessage());
                    }

                    jdbcTemplate.update(
                        "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, fecha_vencimiento, estado, moneda_codigo, " +
                        "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, sello_recibido, fecha_recepcion, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, '2026-06-18', '2026-07-18', 'EMITIDA', 'USD', ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW())",
                        UUID.randomUUID(),
                        firstEmpresaId,
                        clienteId,
                        "F-000001",
                        new java.math.BigDecimal("1200.00"),
                        new java.math.BigDecimal("156.00"),
                        new java.math.BigDecimal("1356.00"),
                        clienteNombre,
                        clienteNif,
                        clienteDir,
                        "SELLO-DEMO-ACEPTADO"
                    );

                    jdbcTemplate.update(
                        "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, fecha_vencimiento, estado, moneda_codigo, " +
                        "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, '2026-06-15', '2026-07-15', 'PAGADA', 'USD', ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                        UUID.randomUUID(),
                        firstEmpresaId,
                        clienteId,
                        "F-000002",
                        new java.math.BigDecimal("450.00"),
                        new java.math.BigDecimal("58.50"),
                        new java.math.BigDecimal("508.50"),
                        clienteNombre,
                        clienteNif,
                        clienteDir
                    );

                    jdbcTemplate.update(
                        "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, fecha_vencimiento, estado, moneda_codigo, " +
                        "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, '2026-06-10', '2026-07-10', 'BORRADOR', 'USD', ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                        UUID.randomUUID(),
                        firstEmpresaId,
                        clienteId,
                        "F-000003",
                        new java.math.BigDecimal("170.00"),
                        new java.math.BigDecimal("22.10"),
                        new java.math.BigDecimal("192.10"),
                        clienteNombre,
                        clienteNif,
                        clienteDir
                    );
                    System.out.println("Facturas de prueba sembradas exitosamente.");
                }
            }

            // 2.5 Usuarios Seeding
            System.out.println("Sembrando usuarios de prueba...");
            try {
                String demoUserPassword = System.getenv("DEMO_USER_PASSWORD");
                if (demoUserPassword == null || demoUserPassword.isBlank()) {
                    throw new IllegalStateException("DEMO_USER_PASSWORD no esta definido; se omite el seed de usuarios demo.");
                }

                UUID selectosId = UUID.fromString("2a1a1661-d7a8-4e89-8d1b-85cf7510d9fa");
                try {
                    String idStr = jdbcTemplate.queryForObject("SELECT CAST(id AS VARCHAR) FROM empresas WHERE nit = ?", String.class, "0614-120392-101-4");
                    if (idStr != null) selectosId = UUID.fromString(idStr);
                } catch (Exception e) {}

                UUID tigoId = UUID.fromString("3b2b2772-e8b9-5f9a-9e2c-96df8621e0fb");
                try {
                    String idStr = jdbcTemplate.queryForObject("SELECT CAST(id AS VARCHAR) FROM empresas WHERE nit = ?", String.class, "0614-250888-102-5");
                    if (idStr != null) tigoId = UUID.fromString(idStr);
                } catch (Exception e) {}

                Integer countSelectos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, "admin@selectos.com");
                if (countSelectos == null || countSelectos == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO usuarios (id, nombre, email, password_hash, es_admin, rol, activo, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, true, 'SUPERADMIN', true, NOW(), NOW())",
                        UUID.fromString("1f2e3d4c-5b6a-7f8e-9d0c-1b2a3f4e5a6b"),
                        "Administrador Selectos",
                        "admin@selectos.com",
                        passwordEncoder.encode(demoUserPassword)
                    );
                    jdbcTemplate.update("INSERT INTO usuario_empresas (usuario_id, empresa_id) VALUES (?, ?) ON CONFLICT DO NOTHING", 
                        UUID.fromString("1f2e3d4c-5b6a-7f8e-9d0c-1b2a3f4e5a6b"), selectosId);
                } else {
                    jdbcTemplate.update("UPDATE usuarios SET password_hash = ? WHERE email = ?", passwordEncoder.encode(demoUserPassword), "admin@selectos.com");
                }

                Integer countTigo = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, "admin@tigo.com");
                if (countTigo == null || countTigo == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO usuarios (id, nombre, email, password_hash, es_admin, rol, activo, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, true, 'ADMINISTRADOR', true, NOW(), NOW())",
                        UUID.fromString("2f3e4d5c-6b7a-8f9e-0d1c-2b3a4f5e6a7b"),
                        "Administrador Tigo",
                        "admin@tigo.com",
                        passwordEncoder.encode(demoUserPassword)
                    );
                    jdbcTemplate.update("INSERT INTO usuario_empresas (usuario_id, empresa_id) VALUES (?, ?) ON CONFLICT DO NOTHING", 
                        UUID.fromString("2f3e4d5c-6b7a-8f9e-0d1c-2b3a4f5e6a7b"), tigoId);
                } else {
                    jdbcTemplate.update("UPDATE usuarios SET password_hash = ? WHERE email = ?", passwordEncoder.encode(demoUserPassword), "admin@tigo.com");
                }

                Integer countEmpleado = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, "empleado@tigo.com");
                if (countEmpleado == null || countEmpleado == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO usuarios (id, nombre, email, password_hash, es_admin, rol, activo, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, false, 'USUARIO', true, NOW(), NOW())",
                        UUID.fromString("3f4e5d6c-7b8a-9f0e-1d2c-3b4a5f6e7a8b"),
                        "Empleado Tigo",
                        "empleado@tigo.com",
                        passwordEncoder.encode(demoUserPassword)
                    );
                    jdbcTemplate.update("INSERT INTO usuario_empresas (usuario_id, empresa_id) VALUES (?, ?) ON CONFLICT DO NOTHING", 
                        UUID.fromString("3f4e5d6c-7b8a-9f0e-1d2c-3b4a5f6e7a8b"), tigoId);
                } else {
                    jdbcTemplate.update("UPDATE usuarios SET password_hash = ? WHERE email = ?", passwordEncoder.encode(demoUserPassword), "empleado@tigo.com");
                }

                System.out.println("Usuarios de prueba sembrados exitosamente.");
            } catch (Exception e) {
                System.out.println("No se pudo sembrar usuarios: " + e.getMessage());
            }

            System.out.println("==================================================");
            System.out.println("--- DATABASE MIGRATION & SEEDING COMPLETED ---");
            System.out.println("==================================================");
            
        } catch (Exception e) {
            System.err.println("Database migration failed: " + e.getMessage());
        }
    }

    private void migrateInvoiceStatusEnum() {
        addInvoiceStatusValue("BORRADOR");
        addInvoiceStatusValue("LISTA_PARA_EMITIR");
        addInvoiceStatusValue("ENVIANDO");
        addInvoiceStatusValue("EMITIDA");
        addInvoiceStatusValue("RECHAZADA");
        addInvoiceStatusValue("CONTINGENCIA");
        addInvoiceStatusValue("ANULADA");
        addInvoiceStatusValue("PAGADA");
    }

    private void addInvoiceStatusValue(String value) {
        try {
            jdbcTemplate.execute("ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS '" + value + "'");
        } catch (Exception e) {
            // La instalacion puede usar VARCHAR en lugar del tipo PostgreSQL invoice_status.
        }
    }

    private void hardenDataIntegrityConstraints() {
        System.out.println("Aplicando restricciones e indices de integridad...");

        executeIgnoringError("UPDATE usuarios SET email = lower(trim(email)) WHERE email IS NOT NULL");
        executeIgnoringError("UPDATE clientes SET email = lower(trim(email)) WHERE email IS NOT NULL");
        executeIgnoringError("ALTER TABLE empresa_monedas ADD COLUMN IF NOT EXISTS principal BOOLEAN DEFAULT false");
        executeIgnoringError("UPDATE empresa_monedas SET principal = false WHERE principal IS NULL");
        executeIgnoringError("WITH primeras AS (SELECT empresa_id, MIN(moneda_codigo) AS moneda_codigo FROM empresa_monedas GROUP BY empresa_id) UPDATE empresa_monedas em SET principal = true FROM primeras p WHERE em.empresa_id = p.empresa_id AND em.moneda_codigo = p.moneda_codigo AND NOT EXISTS (SELECT 1 FROM empresa_monedas x WHERE x.empresa_id = em.empresa_id AND x.principal = true)");

        executeIgnoringError("ALTER TABLE clientes DROP CONSTRAINT IF EXISTS unique_nif_cif");

        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_empresas_nit ON empresas (nit) WHERE nit IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_empresas_email ON empresas (lower(email)) WHERE email IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_empresas_telefono ON empresas (telefono) WHERE telefono IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_email_normalizado ON usuarios (lower(email))");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_clientes_empresa_nif_cif ON clientes (empresa_id, lower(nif_cif)) WHERE deleted_at IS NULL AND nif_cif IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_clientes_empresa_email ON clientes (empresa_id, lower(email)) WHERE deleted_at IS NULL AND email IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_items_empresa_nombre ON items (empresa_id, lower(nombre)) WHERE deleted_at IS NULL AND nombre IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_items_empresa_codigo_interno ON items (empresa_id, lower(codigo_interno)) WHERE deleted_at IS NULL AND codigo_interno IS NOT NULL");
        executeIgnoringError("DROP INDEX IF EXISTS ux_iva_tasas_empresa_nombre");
        executeIgnoringError("DROP INDEX IF EXISTS ux_iva_tasas_empresa_porcentaje");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_iva_tasas_empresa_nombre_activa ON iva_tasas (empresa_id, lower(nombre)) WHERE deleted_at IS NULL AND nombre IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_iva_tasas_empresa_porcentaje_activa ON iva_tasas (empresa_id, porcentaje) WHERE deleted_at IS NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_dte_secuencias_empresa_tipo ON dte_secuencias (empresa_id, tipo_dte)");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_facturas_empresa_numero ON facturas (empresa_id, numero)");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_facturas_codigo_generacion ON facturas (codigo_generacion) WHERE codigo_generacion IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_facturas_numero_control ON facturas (numero_control) WHERE numero_control IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_intentos_emision_idempotency ON intentos_emision (factura_id, idempotency_key) WHERE idempotency_key IS NOT NULL");
        executeIgnoringError("CREATE UNIQUE INDEX IF NOT EXISTS ux_empresa_monedas_principal ON empresa_monedas (empresa_id) WHERE principal = true");

        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_clientes_empresa ON clientes (empresa_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_clientes_empresa_nombre ON clientes (empresa_id, lower(nombre_razon_social))");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_items_empresa ON items (empresa_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_items_iva ON items (iva_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_iva_tasas_empresa ON iva_tasas (empresa_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_facturas_empresa ON facturas (empresa_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_facturas_cliente ON facturas (cliente_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_factura_lineas_factura ON factura_lineas (factura_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_factura_lineas_item ON factura_lineas (item_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_intentos_emision_factura ON intentos_emision (factura_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_usuario_empresas_empresa ON usuario_empresas (empresa_id)");
        executeIgnoringError("CREATE INDEX IF NOT EXISTS idx_empresa_monedas_moneda ON empresa_monedas (moneda_codigo)");

        executeIgnoringError("ALTER TABLE usuario_empresas ADD CONSTRAINT pk_usuario_empresas PRIMARY KEY (usuario_id, empresa_id)");
        executeIgnoringError("ALTER TABLE empresa_monedas ADD CONSTRAINT pk_empresa_monedas PRIMARY KEY (empresa_id, moneda_codigo)");

        executeIgnoringError("ALTER TABLE clientes ADD CONSTRAINT fk_clientes_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID");
        executeIgnoringError("ALTER TABLE iva_tasas ADD CONSTRAINT fk_iva_tasas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID");
        executeIgnoringError("ALTER TABLE items ADD CONSTRAINT fk_items_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID");
        executeIgnoringError("ALTER TABLE items ADD CONSTRAINT fk_items_iva FOREIGN KEY (iva_id) REFERENCES iva_tasas(id) NOT VALID");
        executeIgnoringError("ALTER TABLE facturas ADD CONSTRAINT fk_facturas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID");
        executeIgnoringError("ALTER TABLE facturas ADD CONSTRAINT fk_facturas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) NOT VALID");
        executeIgnoringError("ALTER TABLE facturas ADD CONSTRAINT fk_facturas_moneda FOREIGN KEY (moneda_codigo) REFERENCES monedas(codigo) NOT VALID");
        executeIgnoringError("ALTER TABLE factura_lineas ADD CONSTRAINT fk_factura_lineas_factura FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE CASCADE NOT VALID");
        executeIgnoringError("ALTER TABLE factura_lineas ADD CONSTRAINT fk_factura_lineas_item FOREIGN KEY (item_id) REFERENCES items(id) NOT VALID");
        executeIgnoringError("ALTER TABLE dte_secuencias ADD CONSTRAINT fk_dte_secuencias_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID");
        executeIgnoringError("ALTER TABLE intentos_emision ADD CONSTRAINT fk_intentos_emision_factura FOREIGN KEY (factura_id) REFERENCES facturas(id) NOT VALID");
        executeIgnoringError("ALTER TABLE intentos_emision ADD CONSTRAINT fk_intentos_emision_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) NOT VALID");
        executeIgnoringError("ALTER TABLE usuario_empresas ADD CONSTRAINT fk_usuario_empresas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE NOT VALID");
        executeIgnoringError("ALTER TABLE usuario_empresas ADD CONSTRAINT fk_usuario_empresas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE NOT VALID");
        executeIgnoringError("ALTER TABLE empresa_monedas ADD CONSTRAINT fk_empresa_monedas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE NOT VALID");
        executeIgnoringError("ALTER TABLE empresa_monedas ADD CONSTRAINT fk_empresa_monedas_moneda FOREIGN KEY (moneda_codigo) REFERENCES monedas(codigo) NOT VALID");

        executeIgnoringError("ALTER TABLE clientes ALTER COLUMN empresa_id SET NOT NULL");
        executeIgnoringError("ALTER TABLE empresa_monedas ALTER COLUMN principal SET DEFAULT false");
        executeIgnoringError("ALTER TABLE empresa_monedas ALTER COLUMN principal SET NOT NULL");
        executeIgnoringError("ALTER TABLE clientes ALTER COLUMN nombre_razon_social SET NOT NULL");
        executeIgnoringError("ALTER TABLE clientes ALTER COLUMN nif_cif SET NOT NULL");
        executeIgnoringError("ALTER TABLE clientes ALTER COLUMN activo SET DEFAULT true");
        executeIgnoringError("UPDATE clientes SET activo = true WHERE activo IS NULL");
        executeIgnoringError("ALTER TABLE clientes ALTER COLUMN activo SET NOT NULL");
        executeIgnoringError("ALTER TABLE iva_tasas ALTER COLUMN empresa_id SET NOT NULL");
        executeIgnoringError("ALTER TABLE iva_tasas ALTER COLUMN nombre SET NOT NULL");
        executeIgnoringError("ALTER TABLE iva_tasas ALTER COLUMN porcentaje SET NOT NULL");
        executeIgnoringError("ALTER TABLE iva_tasas ALTER COLUMN activo SET DEFAULT true");
        executeIgnoringError("UPDATE iva_tasas SET activo = true WHERE activo IS NULL");
        executeIgnoringError("ALTER TABLE iva_tasas ALTER COLUMN activo SET NOT NULL");
        executeIgnoringError("ALTER TABLE items ALTER COLUMN empresa_id SET NOT NULL");
        executeIgnoringError("ALTER TABLE items ALTER COLUMN nombre SET NOT NULL");
        executeIgnoringError("ALTER TABLE items ALTER COLUMN categoria SET NOT NULL");
        executeIgnoringError("ALTER TABLE items ALTER COLUMN precio_sin_iva SET NOT NULL");
        executeIgnoringError("ALTER TABLE items ALTER COLUMN unidad_medida SET DEFAULT 59");
        executeIgnoringError("UPDATE items SET unidad_medida = 59 WHERE unidad_medida IS NULL");
        executeIgnoringError("ALTER TABLE items ALTER COLUMN activo SET DEFAULT true");
        executeIgnoringError("UPDATE items SET activo = true WHERE activo IS NULL");
        executeIgnoringError("ALTER TABLE items ALTER COLUMN activo SET NOT NULL");
        executeIgnoringError("ALTER TABLE facturas ALTER COLUMN empresa_id SET NOT NULL");
        executeIgnoringError("ALTER TABLE facturas ALTER COLUMN numero SET NOT NULL");
        executeIgnoringError("ALTER TABLE facturas ALTER COLUMN fecha_emision SET NOT NULL");
        executeIgnoringError("ALTER TABLE facturas ALTER COLUMN estado SET NOT NULL");
        executeIgnoringError("ALTER TABLE facturas ALTER COLUMN moneda_codigo SET NOT NULL");
        executeIgnoringError("UPDATE facturas SET subtotal_sin_iva = 0 WHERE subtotal_sin_iva IS NULL");
        executeIgnoringError("UPDATE facturas SET total_iva = 0 WHERE total_iva IS NULL");
        executeIgnoringError("UPDATE facturas SET total_con_iva = 0 WHERE total_con_iva IS NULL");
        executeIgnoringError("ALTER TABLE facturas ALTER COLUMN subtotal_sin_iva SET NOT NULL");
        executeIgnoringError("ALTER TABLE facturas ALTER COLUMN total_iva SET NOT NULL");
        executeIgnoringError("ALTER TABLE facturas ALTER COLUMN total_con_iva SET NOT NULL");
        executeIgnoringError("ALTER TABLE factura_lineas ALTER COLUMN factura_id SET NOT NULL");
        executeIgnoringError("ALTER TABLE factura_lineas ALTER COLUMN descripcion SET NOT NULL");
        executeIgnoringError("ALTER TABLE factura_lineas ALTER COLUMN cantidad SET NOT NULL");
        executeIgnoringError("ALTER TABLE factura_lineas ALTER COLUMN precio_sin_iva SET NOT NULL");
        executeIgnoringError("ALTER TABLE factura_lineas ALTER COLUMN iva_porcentaje SET NOT NULL");
        executeIgnoringError("ALTER TABLE factura_lineas ALTER COLUMN subtotal_sin_iva SET NOT NULL");
        executeIgnoringError("ALTER TABLE factura_lineas ALTER COLUMN total_iva SET NOT NULL");
        executeIgnoringError("ALTER TABLE factura_lineas ALTER COLUMN total_con_iva SET NOT NULL");
    }

    private void executeIgnoringError(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.out.println("No se aplico migracion de integridad: " + e.getMessage());
        }
    }
}
