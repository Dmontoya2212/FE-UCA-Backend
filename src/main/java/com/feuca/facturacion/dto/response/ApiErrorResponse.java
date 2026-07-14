package com.feuca.facturacion.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> fieldErrors;
    private String traceId;
    private LocalDate date = LocalDate.now();
    private String uri;
    private Object data;
}
