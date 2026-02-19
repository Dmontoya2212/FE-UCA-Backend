package com.feuca.facturacion.exception;

import com.feuca.facturacion.dto.response.ApiErrorResponse;
import com.feuca.facturacion.exception.Empresa.*;
import org.hibernate.dialect.temptable.SQLServerLocalTemporaryTableStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValueOfEntity(
            MethodArgumentNotValidException e
    ) {
        List<String> errors = e.getFieldErrors().stream().map(
                error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, errors);
    }

    public ResponseEntity<ApiErrorResponse> buildErrorResponse(
            Exception e,
            HttpStatus status,
            Object data
    ) {
        String uri = ServletUriComponentsBuilder.fromCurrentRequestUri().build().getPath();
        return ResponseEntity.status(status).body(ApiErrorResponse.builder()
                .data(data)
                .status(status.value())
                .date(LocalDate.now())
                .uri(uri)
                .build()
        );
    }
}
