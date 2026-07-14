ALTER TABLE empresas
    ALTER COLUMN password_hash TYPE TEXT,
    ALTER COLUMN clave_primaria TYPE TEXT;

COMMENT ON COLUMN empresas.password_hash IS
    'Credencial de integracion de empresa cifrada reversiblemente; no es password de login de usuario.';

COMMENT ON COLUMN empresas.clave_primaria IS
    'Clave primaria de integracion cifrada reversiblemente cuando debe reutilizarse ante Hacienda.';

COMMENT ON COLUMN empresas.token IS
    'Token de integracion cifrado reversiblemente; no almacenar tokens sensibles en logs ni DTOs.';
