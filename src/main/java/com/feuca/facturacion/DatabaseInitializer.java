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
        System.out.println("--- RUNNING MANUAL DATABASE MIGRATION FOR EMPRESAS ---");
        System.out.println("==================================================");
        try {
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
            
            // Constraints for empresas
            try { jdbcTemplate.execute("ALTER TABLE empresas ADD CONSTRAINT unique_nit UNIQUE (nit)"); } catch(Exception e) {}
            try { jdbcTemplate.execute("ALTER TABLE empresas ADD CONSTRAINT unique_email UNIQUE (email)"); } catch(Exception e) {}
            try { jdbcTemplate.execute("ALTER TABLE empresas ADD CONSTRAINT unique_telefono UNIQUE (telefono)"); } catch(Exception e) {}

            System.out.println("Migraciones de columnas de empresas realizadas.");

            // Migración para facturas y factura_lineas
            System.out.println("Running database migration for facturas and factura_lineas...");
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

            // Asegurar que las columnas existan si la tabla ya existía
            try {
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS estado VARCHAR(50)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS moneda_codigo VARCHAR(10)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS subtotal_sin_iva NUMERIC(19, 2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS total_iva NUMERIC(19, 2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS total_con_iva NUMERIC(19, 2)");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE facturas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE");

                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS subtotal_sin_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS total_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS total_con_iva NUMERIC(18, 8)");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE");
                jdbcTemplate.execute("ALTER TABLE factura_lineas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE");
            } catch (Exception e) {
                System.out.println("Error al alterar columnas de facturas/factura_lineas: " + e.getMessage());
            }

            // SEED 3 NEW COMPANIES
            System.out.println("Sembrando 3 nuevas empresas de prueba...");
            
            // Empresa 1: Super Selectos
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

            // Empresa 2: Tigo El Salvador
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

            // Empresa 3: Torre Futura Offices
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

            // SEED CLIENTES Linked to first company
            System.out.println("Sembrando 3 clientes de prueba...");
            try {
                jdbcTemplate.execute("ALTER TABLE clientes ADD CONSTRAINT unique_nif_cif UNIQUE (nif_cif)");
            } catch (Exception e) {
                // Constraint might already exist
            }

            // Get first company id (either one of our seeded ones or any existing)
            UUID firstEmpresaId = null;
            try {
                firstEmpresaId = jdbcTemplate.queryForObject("SELECT id FROM empresas LIMIT 1", UUID.class);
            } catch (Exception e) {
                System.out.println("No se pudo obtener empresaId para clientes: " + e.getMessage());
            }

            if (firstEmpresaId != null) {
                // Cliente 1
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

                // Cliente 2
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

                // Cliente 3
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
                System.out.println("Clientes sembrados exitosamente.");

                // SEED ITEMS (Servicios / Productos)
                System.out.println("Sembrando 3 items de prueba...");
                try {
                    // Get or create standard 13% IVA rate for the first empresa
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

                    // Check if items are already seeded
                    Integer itemsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items WHERE empresa_id = ?", Integer.class, firstEmpresaId);
                    if (itemsCount != null && itemsCount == 0) {
                        // Item 1: Servicio
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

                        // Item 2: Producto
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

                        // Item 3: Consultoria
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
                        try {
                            java.nio.file.Files.writeString(
                                java.nio.file.Path.of("c:\\Users\\Familia Montoya\\Documents\\FE-UCA\\db_debug.txt"),
                                "Items sembrados exitosamente para empresa ID: " + firstEmpresaId
                            );
                        } catch (Exception ex) {}
                    } else {
                        try {
                            java.nio.file.Files.writeString(
                                java.nio.file.Path.of("c:\\Users\\Familia Montoya\\Documents\\FE-UCA\\db_debug.txt"),
                                "Items count is not 0: " + itemsCount
                            );
                        } catch (Exception ex) {}
                    }

                    // SEED FACTURAS (Ejemplos)
                    Integer facturasCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM facturas WHERE numero = 'F-000001'", Integer.class);
                    if (facturasCount != null && facturasCount == 0) {
                        System.out.println("Sembrando 3 facturas de prueba...");
                        
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

                        // Factura 1: Emitida
                        UUID fact1Id = UUID.randomUUID();
                        jdbcTemplate.update(
                            "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, estado, moneda_codigo, " +
                            "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, '2026-06-18', 'EMITIDA', 'USD', ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                            fact1Id,
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

                        // Factura 2: Pagada
                        UUID fact2Id = UUID.randomUUID();
                        jdbcTemplate.update(
                            "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, estado, moneda_codigo, " +
                            "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, '2026-06-15', 'PAGADA', 'USD', ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                            fact2Id,
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

                        // Factura 3: Borrador
                        UUID fact3Id = UUID.randomUUID();
                        jdbcTemplate.update(
                            "INSERT INTO facturas (id, empresa_id, cliente_id, numero, fecha_emision, estado, moneda_codigo, " +
                            "subtotal_sin_iva, total_iva, total_con_iva, cliente_nombre_razon_social, cliente_nif_cif, cliente_direccion, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, '2026-06-10', 'BORRADOR', 'USD', ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                            fact3Id,
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
                } catch (Exception e) {
                    System.out.println("No se pudo sembrar items: " + e.getMessage());
                    String details = e.getMessage();
                    if (e.getCause() != null) {
                        details += "\nCause: " + e.getCause().getMessage();
                        if (e.getCause().getCause() != null) {
                            details += "\nRoot Cause: " + e.getCause().getCause().getMessage();
                        }
                    }
                    try {
                        java.nio.file.Files.writeString(
                            java.nio.file.Path.of("c:\\Users\\Familia Montoya\\Documents\\FE-UCA\\db_debug.txt"),
                            "Error sembrando items: " + details
                        );
                    } catch (Exception ex) {}
                }
            }

            // MIGRACIÓN Y SEMBRADO DE USUARIOS DE PRUEBA
            System.out.println("Creando tabla usuarios si no existe...");
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id UUID PRIMARY KEY," +
                    "empresa_id UUID NOT NULL," +
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

            System.out.println("Sembrando usuarios de prueba...");
            try {
                // Buscar UUID reales de las empresas para evitar violaciones de clave foránea
                UUID selectosId = UUID.fromString("2a1a1661-d7a8-4e89-8d1b-85cf7510d9fa");
                try {
                    String idStr = jdbcTemplate.queryForObject(
                        "SELECT CAST(id AS VARCHAR) FROM empresas WHERE nit = ?",
                        String.class,
                        "0614-120392-101-4"
                    );
                    if (idStr != null) {
                        selectosId = UUID.fromString(idStr);
                    }
                } catch (Exception e) {
                    // Ignorar y usar fallback por defecto
                }

                UUID tigoId = UUID.fromString("3b2b2772-e8b9-5f9a-9e2c-96df8621e0fb");
                try {
                    String idStr = jdbcTemplate.queryForObject(
                        "SELECT CAST(id AS VARCHAR) FROM empresas WHERE nit = ?",
                        String.class,
                        "0614-250888-102-5"
                    );
                    if (idStr != null) {
                        tigoId = UUID.fromString(idStr);
                    }
                } catch (Exception e) {
                    // Ignorar y usar fallback por defecto
                }

                // Admin Super Selectos
                Integer countSelectos = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM usuarios WHERE email = ?",
                    Integer.class,
                    "admin@selectos.com"
                );
                if (countSelectos == null || countSelectos == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO usuarios (id, empresa_id, nombre, email, password_hash, es_admin, activo, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, true, true, NOW(), NOW())",
                        UUID.fromString("1f2e3d4c-5b6a-7f8e-9d0c-1b2a3f4e5a6b"),
                        selectosId,
                        "Administrador Selectos",
                        "admin@selectos.com",
                        passwordEncoder.encode("UcaFactura2026.")
                    );
                } else {
                    jdbcTemplate.update(
                        "UPDATE usuarios SET password_hash = ? WHERE email = ?",
                        passwordEncoder.encode("UcaFactura2026."),
                        "admin@selectos.com"
                    );
                }

                // Admin Tigo El Salvador
                Integer countTigo = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM usuarios WHERE email = ?",
                    Integer.class,
                    "admin@tigo.com"
                );
                if (countTigo == null || countTigo == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO usuarios (id, empresa_id, nombre, email, password_hash, es_admin, activo, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, true, true, NOW(), NOW())",
                        UUID.fromString("2f3e4d5c-6b7a-8f9e-0d1c-2b3a4f5e6a7b"),
                        tigoId,
                        "Administrador Tigo",
                        "admin@tigo.com",
                        passwordEncoder.encode("UcaFactura2026.")
                    );
                } else {
                    jdbcTemplate.update(
                        "UPDATE usuarios SET password_hash = ? WHERE email = ?",
                        passwordEncoder.encode("UcaFactura2026."),
                        "admin@tigo.com"
                    );
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
