package com.tareavacaciones.brokermessagebe.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesarPagoDto {
    private String ordenId;
    private Double amount;
    private String paymentMethod;
    private boolean fromRetry;
}