# Roles, Permisos Y Multiempresa

## Roles

### SUPERADMIN

Puede administrar globalmente usuarios, empresas, roles, asignaciones, catalogos, facturas y configuraciones de integracion.

Restricciones:

- No debe dejar el sistema sin `SUPERADMIN` activo.
- No debe eliminar fisicamente facturas emitidas.
- No debe modificar totales calculados, correlativos, codigos DTE o sellos mediante endpoints normales.
- Las acciones sensibles quedan auditadas.

### ADMINISTRADOR

Opera dentro de empresas asignadas.

Puede:

- consultar su empresa
- gestionar clientes, items, IVA y monedas permitidas segun endpoint
- crear y editar facturas en `BORRADOR`
- preparar y enviar DTE
- consultar facturas y estados

No puede:

- crear empresas
- crear o asignar `SUPERADMIN`
- administrar empresas no asignadas
- modificar facturas emitidas
- consultar secretos en texto plano

### USUARIO

Operador normal dentro de empresas asignadas.

Puede:

- consultar, crear y actualizar clientes/items segun reglas de negocio
- crear y editar facturas en `BORRADOR`
- consultar facturas
- generar vista preliminar del DTE

No puede:

- gestionar usuarios, roles o empresas
- modificar credenciales de integracion
- enviar a Hacienda por defecto
- modificar facturas emitidas
- acceder a empresas no asignadas

## Reglas multiempresa

- El frontend puede enviar `empresaId` por ruta, pero el backend siempre valida acceso contra empresas asignadas al usuario.
- `SUPERADMIN` puede acceder a cualquier empresa.
- `ADMINISTRADOR` y `USUARIO` solo pueden acceder a empresas en `usuario_empresas`.
- Los servicios validan tanto el `empresaId` solicitado como el `empresaId` real del recurso.
- Si un recurso no existe dentro del ambito autorizado, se responde como no encontrado cuando aplica para no revelar datos de otra empresa.
- Los mappers no deciden autorizacion.
- No se permite cambiar la empresa propietaria de clientes, items, IVA, lineas o facturas mediante actualizaciones normales.

## Endpoints de usuarios

- Gestion global: `/api/v1/facturacion/usuario`, restringido a `SUPERADMIN`.
- Gestion por empresa: `/api/v1/empresas/{empresaId}/usuarios/**`, restringido por rol y validacion de empresa.
