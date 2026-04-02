package com.tareavacaciones.brokermessagebe.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pago {

    private String id;

    private String ordenId;

    private Double amount;
    private String paymentMethod;
    private String status;
    private String transactionDate;
}