# Flujo De Facturacion, DTE Y Estados

## Estados de factura

| Estado | Significado |
| --- | --- |
| `BORRADOR` | Editable; no es documento emitido. |
| `LISTA_PARA_EMITIR` | DTE generado y validado internamente; no aceptado por Hacienda. |
| `ENVIANDO` | Solicitud enviada o en proceso; pendiente de respuesta definitiva. |
| `EMITIDA` | Documento aceptado por Hacienda con sello o comprobante valido. |
| `RECHAZADA` | Hacienda rechazo el documento o la respuesta valida indica rechazo. |
| `CONTINGENCIA` | Documento bajo flujo de contingencia autorizado. |
| `ANULADA` | Documento invalidado mediante flujo legal. |
| `PAGADA` | Documento emitido registrado como pagado. |

## Transiciones principales

- `BORRADOR -> LISTA_PARA_EMITIR`
- `BORRADOR -> ENVIANDO`
- `LISTA_PARA_EMITIR -> ENVIANDO`
- `RECHAZADA -> ENVIANDO`, si se permite reintento
- `ENVIANDO -> EMITIDA`
- `ENVIANDO -> RECHAZADA`
- `EMITIDA -> PAGADA`
- `EMITIDA -> ANULADA`, mediante proceso legal

No permitir:

- `BORRADOR -> PAGADA`
- `RECHAZADA -> PAGADA`
- `EMITIDA -> BORRADOR`
- `ANULADA -> EMITIDA`

## Flujo de emision

1. Crear factura en `BORRADOR`.
2. Agregar o actualizar lineas.
3. Recalcular totales de lineas y factura en backend.
4. Generar o preparar DTE con datos validados.
5. Validar estructura JSON y reglas tributarias.
6. Firmar documento cuando aplique.
7. Registrar intento de emision.
8. Enviar a Hacienda.
9. Guardar request/response de forma segura.
10. Si Hacienda acepta, guardar sello, fecha de recepcion y cambiar a `EMITIDA`.
11. Si Hacienda rechaza, guardar errores y cambiar a `RECHAZADA`.

## Reglas financieras

- El frontend no es fuente confiable de `subtotalSinIva`, `totalIva` ni `totalConIva`.
- Las lineas se calculan con `BigDecimal`.
- Cualquier alta, actualizacion o eliminacion de linea recalcula la factura.
- Una factura sin lineas no puede emitirse.
- Los snapshots de cliente, item, emisor, IVA y moneda preservan la historia del documento.

## Evidencia de emision

La evidencia se conserva en:

- `facturas`: estado final, sello, respuesta Hacienda, codigo de generacion, numero de control.
- `intentos_emision`: intentos, request, response, errores tecnicos e idempotencia.

Una factura no debe considerarse `EMITIDA` sin respuesta aceptada de Hacienda.
