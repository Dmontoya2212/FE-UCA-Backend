# Preparacion Para Produccion

## Seguridad operativa

- HTTPS debe estar activo en produccion. El perfil `prod` fija `security.require-https=true`; si la aplicacion corre detras de proxy o balanceador, configurar `X-Forwarded-Proto` correctamente para que Spring detecte la solicitud como segura.
- CORS se configura con `CORS_ALLOWED_ORIGINS` y no debe usar comodines en produccion.
- El login tiene rate limit por IP con `LOGIN_RATE_LIMIT_MAX_ATTEMPTS` y `LOGIN_RATE_LIMIT_WINDOW_SECONDS`.
- La emision de DTE tiene rate limit por IP y factura con `EMISSION_RATE_LIMIT_MAX_ATTEMPTS` y `EMISSION_RATE_LIMIT_WINDOW_SECONDS`.
- Los secretos (`JWT_SECRET`, `SECRET_ENCRYPTION_KEY`, credenciales de BD y Hacienda) deben venir de variables de entorno o gestor de secretos. No deben guardarse en repositorio ni logs.
- Rotar `JWT_SECRET` y `SECRET_ENCRYPTION_KEY` mediante una ventana operativa controlada. Para la llave de cifrado, planificar re-cifrado o soporte temporal de llave anterior antes de reemplazarla.
- Usar un usuario de BD con privilegios minimos: lectura/escritura solo sobre el esquema de la aplicacion y permisos de migracion separados cuando sea posible.
- Configurar backups automaticos de la BD, probar restauracion y conservar evidencia de la ultima prueba de restore.
- Ejecutar revision de dependencias antes de liberar: `mvn versions:display-dependency-updates` y una herramienta de vulnerabilidades como OWASP Dependency Check o el escaner corporativo.
- Mantener `hacienda.simulation-enabled=false` en produccion.
- El perfil `prod` desactiva mensajes detallados de error, stack traces, SQL debug y reinicio devtools.

## Salud y monitoreo

- Actuator expone `health`, `info` y `metrics`; `health` e `info` pueden usarse para probes, y el resto de `/actuator/**` requiere `SUPERADMIN`.
- Monitorear `health` para disponibilidad y conexion de BD.
- Monitorear metricas HTTP de Actuator para errores 5xx y latencias generales.
- Monitorear `facturacion.auth.failures` para fallos de autenticacion.
- Monitorear `facturacion.emision.failures` y `facturacion.hacienda.latency` para fallos y latencia de Hacienda.
- Monitorear `facturacion.facturas.enviando` y `facturacion.facturas.enviando.stale`; el umbral se configura con `MONITORING_SENDING_STALE_MINUTES`.
- Monitorear `facturacion.correlativos.secuencias` y `facturacion.correlativos.consumidos`.
- Para uso de recursos, conectar Actuator al recolector del entorno (Prometheus, APM corporativo o plataforma cloud) y alertar por memoria, CPU, pool JDBC y saturacion de hilos.

## Respaldo y recuperacion

- El procedimiento operativo esta definido en `docs/backup-recovery.md`.
- Frecuencia minima: backup completo diario, incrementales o WAL cada 15 minutos y snapshot previo a migraciones.
- Retencion minima: diarios 30 dias, semanales 12 semanas y mensuales 12 meses.
- La evidencia DTE se recupera desde `facturas` e `intentos_emision`.
- Una factura en `ENVIANDO` despues de una caida no debe marcarse como `EMITIDA` sin confirmacion oficial de Hacienda.
