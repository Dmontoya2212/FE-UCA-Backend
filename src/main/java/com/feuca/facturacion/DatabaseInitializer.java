package com.feuca.facturacion;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
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
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE empresas ADD COLUMN IF NOT EXISTS clave_primaria VARCHAR(255)");
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
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS moneda_codigo VARCHAR(10)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS subtotal_sin_iva NUMERIC(19, 2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS total_iva NUMERIC(19, 2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS total_con_iva NUMERIC(19, 2)");
                
                // Cliente denormalization
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_nombre_razon_social VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_nif_cif VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS cliente_direccion VARCHAR(255)");
                
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE");

                // DTE Facturas
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS numero_control VARCHAR(31)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS codigo_generacion VARCHAR(36)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS condicion_operacion INTEGER DEFAULT 1");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS sello_recibido VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS fecha_recepcion TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS tipo_dte VARCHAR(2) DEFAULT '01'");

                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS subtotal_sin_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS total_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS total_con_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE");
            } catch (Exception e) {
                System.out.println("Error al alterar columnas de facturas/factura_lineas: " + e.getMessage());
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
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, NOW(), NOW()) ON CONFLICT (nif_cif) DO NOTHING",
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
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, NOW(), NOW()) ON CONFLICT (nif_cif) DO NOTHING",
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
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, NOW(), NOW()) ON CONFLICT (nif_cif) DO NOTHING",
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
                        "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, estado, moneda_codigo, " +
                        "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, '2026-06-18', 'EMITIDA', 'USD', ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                        UUID.randomUUID(),
                        firstEmpresaId,
                        clienteId,
                        "F-000001",
                        new java.math.BigDecimal("1200.00"),
                        new java.math.BigDecimal("156.00"),
                        new java.math.BigDecimal("1356.00"),
                        clienteNombre,
                        clienteNif,
                        clienteDir
                    );

                    jdbcTemplate.update(
                        "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, estado, moneda_codigo, " +
                        "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, '2026-06-15', 'PAGADA', 'USD', ?, ?, ?, ?, ?, ?, NOW(), NOW())",
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
                        "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, estado, moneda_codigo, " +
                        "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, '2026-06-10', 'BORRADOR', 'USD', ?, ?, ?, ?, ?, ?, NOW(), NOW())",
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
                        passwordEncoder.encode("UcaFactura2026.")
                    );
                    jdbcTemplate.update("INSERT INTO usuario_empresas (usuario_id, empresa_id) VALUES (?, ?) ON CONFLICT DO NOTHING", 
                        UUID.fromString("1f2e3d4c-5b6a-7f8e-9d0c-1b2a3f4e5a6b"), selectosId);
                } else {
                    jdbcTemplate.update("UPDATE usuarios SET password_hash = ? WHERE email = ?", passwordEncoder.encode("UcaFactura2026."), "admin@selectos.com");
                }

                Integer countTigo = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, "admin@tigo.com");
                if (countTigo == null || countTigo == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO usuarios (id, nombre, email, password_hash, es_admin, rol, activo, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, true, 'ADMINISTRADOR', true, NOW(), NOW())",
                        UUID.fromString("2f3e4d5c-6b7a-8f9e-0d1c-2b3a4f5e6a7b"),
                        "Administrador Tigo",
                        "admin@tigo.com",
                        passwordEncoder.encode("UcaFactura2026.")
                    );
                    jdbcTemplate.update("INSERT INTO usuario_empresas (usuario_id, empresa_id) VALUES (?, ?) ON CONFLICT DO NOTHING", 
                        UUID.fromString("2f3e4d5c-6b7a-8f9e-0d1c-2b3a4f5e6a7b"), tigoId);
                } else {
                    jdbcTemplate.update("UPDATE usuarios SET password_hash = ? WHERE email = ?", passwordEncoder.encode("UcaFactura2026."), "admin@tigo.com");
                }

                Integer countEmpleado = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, "empleado@tigo.com");
                if (countEmpleado == null || countEmpleado == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO usuarios (id, nombre, email, password_hash, es_admin, rol, activo, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, false, 'USUARIO', true, NOW(), NOW())",
                        UUID.fromString("3f4e5d6c-7b8a-9f0e-1d2c-3b4a5f6e7a8b"),
                        "Empleado Tigo",
                        "empleado@tigo.com",
                        passwordEncoder.encode("UcaFactura2026.")
                    );
                    jdbcTemplate.update("INSERT INTO usuario_empresas (usuario_id, empresa_id) VALUES (?, ?) ON CONFLICT DO NOTHING", 
                        UUID.fromString("3f4e5d6c-7b8a-9f0e-1d2c-3b4a5f6e7a8b"), tigoId);
                } else {
                    jdbcTemplate.update("UPDATE usuarios SET password_hash = ? WHERE email = ?", passwordEncoder.encode("UcaFactura2026."), "empleado@tigo.com");
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
}
