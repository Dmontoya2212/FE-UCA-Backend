# FE UCA Facturacion Electronica Backend

Backend Spring Boot para facturacion electronica multiempresa. El sistema gestiona empresas, usuarios, clientes, items, IVA, monedas, facturas, generacion DTE y preparacion/envio hacia Hacienda con aislamiento por empresa, auditoria, migraciones y controles de produccion.

## Documentacion

- [Variables de entorno y perfiles](docs/configuration.md)
- [Roles, permisos y reglas multiempresa](docs/security-model.md)
- [Flujo de emision, estados y DTE](docs/billing-flow.md)
- [Migraciones, pruebas y despliegue](docs/development-deployment.md)
- [Preparacion para produccion](docs/production-readiness.md)
- [Respaldo y recuperacion](docs/backup-recovery.md)
- [OpenAPI inicial](docs/openapi.yaml)

## Requisitos

- Java 21
- PostgreSQL
- Maven Wrapper incluido en el repositorio

## Arranque local

Configurar variables locales seguras antes de iniciar. Como minimo:

```powershell
$env:LOCAL_DB_PASSWORD="..."
$env:LOCAL_JWT_SECRET="clave-de-al-menos-32-bytes"
$env:LOCAL_SECRET_ENCRYPTION_KEY="clave-base64-o-texto-seguro"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

El perfil local usa `application-local.yml`; produccion debe usar `prod` y variables sin valores por defecto sensibles.

## Pruebas

```powershell
.\mvnw.cmd clean test
```

Las pruebas de integracion esperan una BD PostgreSQL disponible segun `TEST_DB_URL`, `TEST_DB_USERNAME` y `TEST_DB_PASSWORD`.

## Seguridad Basica

- Los JWT no sustituyen la validacion de pertenencia a empresa en BD.
- `SUPERADMIN` tiene control global.
- `ADMINISTRADOR` y `USUARIO` solo operan dentro de empresas asignadas.
- Una factura solo puede pasar a `EMITIDA` con respuesta aceptada de Hacienda y evidencia persistida.
- Los secretos se configuran por variables de entorno o gestor externo, no en repositorio.
