-- Única cuenta inicial. La contraseña en texto claro no se almacena en el repositorio.
-- El hash usa BCrypt con factor de coste 12.
INSERT INTO usuarios (
    id,
    nombre,
    email,
    password_hash,
    es_admin,
    rol,
    activo,
    created_at,
    updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000001'::uuid,
    'Super Administrador',
    'superadmin@facturacion.local',
    '$2a$12$q15tdawWXwav9712wKgAVu4YOsdtSzRz3bvZ1LqZUg/agsP7B3md6',
    true,
    'SUPERADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM usuarios
    WHERE lower(email) = 'superadmin@facturacion.local'
);
