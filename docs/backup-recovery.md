# Respaldo Y Recuperacion

## Objetivo

Este procedimiento protege la continuidad del backend de facturacion electronica y la evidencia legal de emision. Cubre base de datos, DTE generados, respuestas de Hacienda, correlativos y recuperacion ante una caida durante el envio.

## Alcance de respaldo

Respaldar como minimo:

- Base de datos PostgreSQL completa.
- Migraciones Flyway versionadas en repositorio.
- Variables y secretos operativos almacenados en gestor de secretos, no dentro del backup de aplicacion.
- Evidencia DTE persistida en `facturas`:
  - `numero_control`
  - `codigo_generacion`
  - `sello_recibido`
  - `fecha_recepcion`
  - `hacienda_codigo_respuesta`
  - `hacienda_mensaje_respuesta`
  - `hacienda_errores`
  - `hacienda_response_json`
- Evidencia de transmision en `intentos_emision`:
  - request enviado
  - response recibido
  - ambiente
  - numero de intento
  - clave de idempotencia
  - error tecnico
  - fechas de intento y respuesta
- Secuencias DTE en `dte_secuencias`.

No incluir en respaldos planos sin cifrar:

- `JWT_SECRET`
- `SECRET_ENCRYPTION_KEY`
- credenciales de BD
- credenciales o tokens de Hacienda
- llaves privadas

## Frecuencia

- Backup completo diario.
- Backup incremental o WAL archiving cada 15 minutos, si la infraestructura lo permite.
- Snapshot previo a migraciones de produccion.
- Backup manual antes de rotacion de llaves, cambios de esquema o despliegues con impacto en emision.

RPO objetivo: 15 minutos.

RTO objetivo inicial: 2 horas.

Estos valores deben ajustarse si el negocio exige menor perdida aceptable o menor tiempo de recuperacion.

## Retencion

- Backups diarios: 30 dias.
- Backups semanales: 12 semanas.
- Backups mensuales: 12 meses.
- Backups previos a migraciones: conservar al menos hasta completar dos cierres contables o el periodo legal definido por la organizacion.
- Evidencia de facturas emitidas y respuestas de Hacienda: conservar segun obligacion legal y politica contable; no aplicar purga automatica sin autorizacion formal.

## Cifrado

- Cifrar backups en reposo con KMS, gestor cloud o herramienta equivalente.
- Cifrar transferencias con TLS.
- Separar llaves de cifrado del almacenamiento de backups.
- Restringir acceso a backups al equipo operativo autorizado.
- Registrar accesos y restauraciones.
- Probar restauracion despues de rotar llaves.

## Restauraciones de prueba

Frecuencia minima:

- Restauracion tecnica mensual en entorno aislado.
- Restauracion completa antes de cada salida mayor a produccion.
- Restauracion posterior a cambios en esquema, cifrado o estrategia de backups.

Checklist de restauracion:

- Levantar PostgreSQL en entorno aislado.
- Restaurar backup completo y WAL/incrementales necesarios.
- Ejecutar validacion de integridad de Flyway.
- Arrancar la aplicacion con perfil de prueba o recuperacion.
- Verificar conteos de tablas criticas: `facturas`, `factura_lineas`, `intentos_emision`, `dte_secuencias`, `usuarios`, `empresas`.
- Verificar que una factura emitida conserva `codigo_generacion`, `numero_control`, `sello_recibido` y response de Hacienda.
- Verificar que una factura rechazada conserva errores y response.
- Verificar que no se exponen secretos en logs durante la restauracion.
- Documentar fecha, responsable, backup utilizado, tiempo de restauracion y resultado.

## Recuperacion de DTE y respuestas

Para reconstruir evidencia de una factura emitida:

1. Consultar `facturas` por `id`, `codigo_generacion` o `numero_control`.
2. Confirmar `estado = EMITIDA`.
3. Extraer `sello_recibido`, `fecha_recepcion`, codigo y mensaje de Hacienda.
4. Consultar `intentos_emision` por `factura_id`.
5. Identificar el intento aceptado por `estado_intento`, `codigo_hacienda`, `sello_recibido` o response.
6. Usar `request_json` y `response_json` como evidencia tecnica del documento enviado y la respuesta recibida.

Si hay mas de un intento:

- El intento aceptado es la evidencia principal.
- Los intentos rechazados o con error tecnico se conservan como trazabilidad.
- No se debe alterar manualmente `codigo_generacion`, `numero_control` o `sello_recibido`.

## Caida durante una emision

Escenario A: la factura queda en `BORRADOR` o `LISTA_PARA_EMITIR`.

- No hay envio en curso registrado como definitivo.
- El usuario autorizado puede reintentar el envio.
- El backend debe volver a validar datos y estado antes de transmitir.

Escenario B: la factura queda en `ENVIANDO` con intento registrado y sin respuesta.

- No marcar manualmente como `EMITIDA`.
- Revisar `intentos_emision` para confirmar si existe `request_json`, `codigo_generacion`, `numero_control` e `idempotency_key`.
- Consultar el estado del documento en Hacienda usando `codigo_generacion` o el mecanismo oficial disponible.
- Si Hacienda confirma aceptacion:
  - registrar response oficial.
  - guardar `sello_recibido` y `fecha_recepcion`.
  - cambiar la factura a `EMITIDA` mediante un procedimiento administrativo auditado.
- Si Hacienda confirma rechazo:
  - registrar errores oficiales.
  - cambiar a `RECHAZADA`.
- Si no se puede confirmar respuesta:
  - mantener o mover a estado reintentable solo con politica documentada.
  - reintentar usando la misma identidad documental cuando aplique.
  - evitar consumir un nuevo correlativo para el mismo documento sin decision legal/operativa.

Escenario C: la factura queda en `ENVIANDO` sin intento registrado.

- Revisar logs por `facturaId`, `codigoGeneracion` y `traceId`.
- Confirmar si hubo llamada saliente a Hacienda.
- Si no hubo evidencia de envio, reintentar desde el flujo normal.
- Si hay duda razonable de envio, tratar como Escenario B y consultar a Hacienda antes de reenviar.

## Verificaciones posteriores a recuperacion

- Ninguna factura `EMITIDA` debe carecer de `sello_recibido` o evidencia equivalente.
- Ninguna factura `ANULADA` debe volver a estados de emision.
- Las facturas en `ENVIANDO` por mas del umbral configurado deben investigarse.
- Los correlativos en `dte_secuencias` no deben retroceder despues de restaurar.
- Las metricas `facturacion.facturas.enviando.stale` y `facturacion.emision.failures` deben quedar bajo monitoreo.

## Responsabilidades

- Operaciones: ejecutar backups, restauraciones y monitoreo.
- Desarrollo: mantener migraciones, compatibilidad de restauracion y pruebas automatizadas.
- Soporte administrativo: validar estados legales de DTE ante Hacienda.
- Seguridad: custodiar llaves, controlar accesos y auditar restauraciones.
