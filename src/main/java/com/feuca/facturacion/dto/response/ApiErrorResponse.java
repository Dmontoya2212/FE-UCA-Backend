package com.feuca.facturacion.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private int status;
    @Builder.Default
    private LocalDate date = LocalDate.now();
    private String uri;
    private Object data;
}
