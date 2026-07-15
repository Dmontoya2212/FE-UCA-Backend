package com.feuca.facturacion.exception;

import com.feuca.facturacion.dto.response.ApiErrorResponse;
import com.feuca.facturacion.config.CorrelationIdFilter;
import com.feuca.facturacion.exception.Empresa.*;
import com.feuca.facturacion.exception.Item.*;
import com.feuca.facturacion.exception.IvaTasa.*;
import com.feuca.facturacion.exception.Cliente.*;
import com.feuca.facturacion.exception.Usuario.*;
import com.feuca.facturacion.exception.Factura.FacturaNotFoundException;
import com.feuca.facturacion.exception.Factura.FacturaAlreadyExistsException;
import com.feuca.facturacion.exception.Factura.FacturaNoEditableException;
import com.feuca.facturacion.exception.Factura.FacturaValidationException;
import com.feuca.facturacion.exception.FacturaLinea.FacturaLineaNotFoundException;
import com.feuca.facturacion.exception.Moneda.MonedaNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //Excepciones de EMPRESA
    @ExceptionHandler(EmpresaAlredyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmpresaAlredyExistsException(EmpresaAlredyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(EmpresaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEmpresaNotFoundException(EmpresaNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(EmpresaEmailAlredyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmpresaEmailAlredyExistsException(EmpresaEmailAlredyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(EmpresaNifCifAlredyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmpresaNifCifAlredyExistsException(EmpresaNifCifAlredyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(EmpresaTelefonoAlredyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmpresaTelefonoAlredyExistsException(EmpresaTelefonoAlredyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    // Excepciones de IVA TASA
    @ExceptionHandler(IvaTasaAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleIvaTasaAlreadyExistsException(IvaTasaAlreadyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IvaTasaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleIvaTasaNotFoundException(IvaTasaNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    // Excepciones de ITEM
    @ExceptionHandler(ItemAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleItemAlreadyExistsException(ItemAlreadyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleItemNotFoundException(ItemNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ItemIvaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleItemIvaNotFoundException(ItemIvaNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    //Excepciones de Cliente
    @ExceptionHandler(ClienteAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleClienteAlreadyExistsException(ClienteAlreadyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleClienteNotFoundException(ClienteNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    //Excepciones de MONEDA
    @ExceptionHandler(MonedaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMonedaNotFoundException(MonedaNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValueOfEntity(
            MethodArgumentNotValidException e
    ) {
        List<String> errors = e.getFieldErrors().stream().map(this::fieldError)
                .toList();

        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "La solicitud no es valida.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException e) {
        return buildErrorResponse(e, HttpStatus.UNAUTHORIZED, "Debe autenticarse para acceder a este recurso.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        return buildErrorResponse(e, HttpStatus.FORBIDDEN, e.getMessage());
    }

    public ResponseEntity<ApiErrorResponse> buildErrorResponse(
            Exception e,
            HttpStatus status,
            Object data
    ) {
        String uri = ServletUriComponentsBuilder.fromCurrentRequestUri().build().getPath();
        List<String> fieldErrors = data instanceof List<?> list
                ? list.stream().filter(String.class::isInstance).map(String.class::cast).toList()
                : null;
        String message = data instanceof String text
                ? text
                : fieldErrors != null
                ? "La solicitud contiene errores de validacion."
                : status.getReasonPhrase();
        return ResponseEntity.status(status).body(ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .data(data)
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(uri)
                .fieldErrors(fieldErrors)
                .traceId(resolveTraceId())
                .date(LocalDate.now())
                .uri(uri)
                .build()
        );
    }
    // Excepciones de USUARIO
    @ExceptionHandler(UsuarioAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsuarioAlreadyExistsException(UsuarioAlreadyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsuarioNotFoundException(UsuarioNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException e) {
        return buildErrorResponse(e, HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // Excepciones Factura

    @ExceptionHandler(FacturaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFacturaNotFoundException(FacturaNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(FacturaAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleFacturaAlreadyExistsException(FacturaAlreadyExistsException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(FacturaNoEditableException.class)
    public ResponseEntity<ApiErrorResponse> handleFacturaNoEditableException(FacturaNoEditableException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(FacturaValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleFacturaValidationException(FacturaValidationException e) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, e.getErrors());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockException(Exception e) {
        return buildErrorResponse(
                e,
                HttpStatus.CONFLICT,
                "La factura fue modificada por otro proceso. Recargue la informacion e intente nuevamente."
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return buildErrorResponse(e, HttpStatus.CONFLICT, duplicateMessage(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception e) {
        log.error("Error inesperado: {}", e.getMessage(), e);
        return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado.");
    }

    // Excepciones Factura Linea

    @ExceptionHandler(FacturaLineaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFacturaLineaNotFoundException(FacturaLineaNotFoundException e) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, e.getMessage());
    }

    private String duplicateMessage(DataIntegrityViolationException e) {
        String detail = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
        if (detail == null) {
            return "La operacion viola una restriccion de integridad de datos.";
        }
        if (detail.contains("ux_usuarios_email_normalizado")) {
            return "Ya existe un usuario con ese email.";
        }
        if (detail.contains("ux_clientes_empresa_nif_cif")) {
            return "Ya existe un cliente con ese NIF/CIF para esta empresa.";
        }
        if (detail.contains("ux_clientes_empresa_email")) {
            return "Ya existe un cliente con ese email para esta empresa.";
        }
        if (detail.contains("ux_items_empresa_nombre")) {
            return "Ya existe un item con ese nombre para esta empresa.";
        }
        if (detail.contains("ux_items_empresa_codigo_interno")) {
            return "Ya existe un item con ese codigo interno para esta empresa.";
        }
        if (detail.contains("ux_iva_tasas_empresa_nombre_activa")) {
            return "Ya existe una tasa de IVA activa con ese nombre para esta empresa.";
        }
        if (detail.contains("ux_iva_tasas_empresa_porcentaje_activa")) {
            return "Ya existe una tasa de IVA activa con ese porcentaje para esta empresa.";
        }
        if (detail.contains("ux_facturas_empresa_numero")) {
            return "Ya existe una factura con ese numero para esta empresa.";
        }
        if (detail.contains("ux_facturas_codigo_generacion")) {
            return "Ya existe una factura con ese codigo de generacion.";
        }
        if (detail.contains("ux_facturas_numero_control")) {
            return "Ya existe una factura con ese numero de control.";
        }
        if (detail.contains("ux_dte_secuencias_empresa_tipo")) {
            return "Ya existe una secuencia DTE para esta empresa y tipo de documento.";
        }
        if (detail.contains("ux_intentos_emision_idempotency")) {
            return "Ya existe un intento de emision con esa clave de idempotencia.";
        }
        if (detail.contains("duplicate key") || detail.contains("llave duplicada")) {
            return "Ya existe un registro con esos datos.";
        }
        return "La operacion viola una restriccion de integridad de datos.";
    }

    private String fieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private String resolveTraceId() {
        String traceId = MDC.get(CorrelationIdFilter.TRACE_ID_MDC_KEY);
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }
}
