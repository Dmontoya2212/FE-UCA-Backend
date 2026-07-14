# Variables De Entorno Y Perfiles

## Perfiles

- `local`: desarrollo local. Permite defaults no productivos para BD local y CORS de frontend local.
- `test`: pruebas automatizadas. Usa variables `TEST_*`.
- `prod`: produccion. Exige secretos sin fallback, HTTPS, CORS explicito, errores sin detalle y simulacion Hacienda apagada.

## Variables obligatorias en produccion

| Variable | Uso |
| --- | --- |
| `DB_URL` | JDBC URL de PostgreSQL. |
| `DB_USERNAME` | Usuario de BD con privilegios minimos. |
| `DB_PASSWORD` | Password de BD. |
| `JWT_SECRET` | Secreto de firma JWT; minimo 32 bytes. |
| `JWT_ISSUER` | Issuer esperado del token. |
| `SECRET_ENCRYPTION_KEY` | Llave para cifrado reversible de credenciales de integracion. |
| `CORS_ALLOWED_ORIGINS` | Origenes frontend autorizados, separados por coma. |
| `DTE_AMBIENTE` | Ambiente DTE, por ejemplo `00` pruebas o `01` produccion segun normativa. |
| `HACIENDA_ENDPOINT` | Endpoint de integracion Hacienda. |

## Variables operativas

| Variable | Default | Uso |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | Puerto HTTP interno. |
| `JWT_EXPIRATION_MS` | `900000` | Vigencia de access token. |
| `SECURITY_REQUIRE_HTTPS` | `false` general, `true` en prod | Fuerza canal seguro. |
| `LOGIN_RATE_LIMIT_MAX_ATTEMPTS` | `5` | Intentos de login por ventana. |
| `LOGIN_RATE_LIMIT_WINDOW_SECONDS` | `300` | Ventana de rate limit de login. |
| `EMISSION_RATE_LIMIT_MAX_ATTEMPTS` | `10` | Intentos de emision por ventana. |
| `EMISSION_RATE_LIMIT_WINDOW_SECONDS` | `60` | Ventana de rate limit de emision. |
| `MONITORING_SENDING_STALE_MINUTES` | `15` | Umbral para facturas `ENVIANDO` estancadas. |
| `DATABASE_INITIALIZER_ENABLED` | `false` | Inicializador legado. Mantener apagado fuera de transicion controlada. |
| `HACIENDA_SIMULATION_ENABLED` | `false` | Simulacion de envio. Nunca activar en prod. |

## Actuator

Se exponen `health`, `info` y `metrics`.

- `/actuator/health` y `/actuator/info`: disponibles para probes.
- Otros endpoints bajo `/actuator/**`: requieren `SUPERADMIN`.

## Secretos

No guardar en repositorio:

- passwords de BD
- JWT secret
- llaves de cifrado
- tokens o credenciales Hacienda
- llaves privadas

Usar variables de entorno, gestor de secretos o mecanismo equivalente del entorno de despliegue.
