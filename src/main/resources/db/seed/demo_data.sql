-- Datos demo para desarrollo local. No se ejecuta automaticamente por Flyway.
-- Ejecutar manualmente solo en entornos de prueba/desarrollo.

INSERT INTO empresas (
    id, razon_social, nombre_legal, nombre_comercial, nit, email, telefono,
    direccion, actividad_economica, ciudad, pais, created_at, updated_at
) VALUES
    (
        '2a1a1661-d7a8-4e89-8d1b-85cf7510d9fa',
        'Corporacion de Alimentos S.A. de C.V.',
        'Corporacion de Alimentos S.A. de C.V.',
        'Super Selectos Escalon',
        '0614-120392-101-4',
        'contacto@selectos.com.sv',
        '+503 2264-8500',
        'Paseo General Escalon y 75 Av. Norte, San Salvador',
        'Comercio de productos de primera necesidad',
        'San Salvador',
        'El Salvador',
        NOW(),
        NOW()
    ),
    (
        '3b2b2772-e8b9-5f9a-9e2c-96df8621e0fb',
        'Telecomunicaciones del Centro S.A. de C.V.',
        'Telecomunicaciones del Centro S.A. de C.V.',
        'Tigo El Salvador',
        '0614-250888-102-5',
        'soporte@tigo.com.sv',
        '+503 2508-0000',
        'Carretera a La Libertad, Km 10, Santa Tecla',
        'Servicios de telecomunicaciones',
        'Santa Tecla',
        'El Salvador',
        NOW(),
        NOW()
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO empresa_monedas (empresa_id, moneda_codigo, principal) VALUES
    ('2a1a1661-d7a8-4e89-8d1b-85cf7510d9fa', 'USD', true),
    ('3b2b2772-e8b9-5f9a-9e2c-96df8621e0fb', 'USD', true)
ON CONFLICT (empresa_id, moneda_codigo) DO UPDATE
SET principal = EXCLUDED.principal;
