# Desarrollo, Migraciones, Pruebas Y Despliegue

## Desarrollo local

1. Instalar Java 21.
2. Levantar PostgreSQL.
3. Configurar variables `LOCAL_*`.
4. Ejecutar:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

## Migraciones

El proyecto usa Flyway:

- Ubicacion: `src/main/resources/db/migration`
- Formato: `V{numero}__descripcion.sql`
- `ddl-auto` esta en `none`.
- `flyway.clean-disabled=true`.
- No usar `DatabaseInitializer` para datos productivos.

Antes de crear una migracion:

- revisar el esquema actual
- evitar cambios destructivos sin plan de rollback
- agregar indices/restricciones cuando sean barreras de integridad
- probar en una copia de datos o entorno aislado

## Pruebas

Ejecutar suite completa:

```powershell
.\mvnw.cmd clean test
```

Variables de prueba:

- `TEST_DB_URL`
- `TEST_DB_USERNAME`
- `TEST_DB_PASSWORD`
- `TEST_JWT_SECRET`
- `TEST_SECRET_ENCRYPTION_KEY`

La suite cubre seguridad, roles, multiempresa, calculos, estados, DTE, migraciones parciales, transacciones, auditoria y validaciones.

## Despliegue

Checklist minimo:

- Perfil `prod` activo.
- `CORS_ALLOWED_ORIGINS` limitado.
- `JWT_SECRET` y `SECRET_ENCRYPTION_KEY` provistos por gestor de secretos.
- HTTPS terminado correctamente y `X-Forwarded-Proto` configurado si hay proxy.
- `HACIENDA_SIMULATION_ENABLED=false`.
- `DATABASE_INITIALIZER_ENABLED=false`.
- Usuario de BD con privilegios minimos.
- Migraciones Flyway ejecutadas y validadas.
- Actuator conectado al sistema de monitoreo.
- Backups probados.

## Recuperacion

El procedimiento esta en [backup-recovery.md](backup-recovery.md).

Puntos criticos:

- no retroceder correlativos
- no marcar `EMITIDA` sin evidencia oficial
- investigar facturas `ENVIANDO` despues de caidas
- restaurar en entorno aislado antes de reemplazar produccion
