package com.feuca.facturacion.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralResponse {
    private String uri;
    private String message;
    private int status;
    private LocalDate date = LocalDate.now();
    private Object data;
}
