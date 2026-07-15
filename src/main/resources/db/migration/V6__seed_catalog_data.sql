INSERT INTO monedas (codigo, nombre, simbolo) VALUES
    ('USD', 'Dolar estadounidense', '$'),
    ('EUR', 'Euro', 'EUR'),
    ('SVC', 'Colon salvadoreno', '₡')
ON CONFLICT (codigo) DO UPDATE
SET nombre = EXCLUDED.nombre,
    simbolo = EXCLUDED.simbolo;
