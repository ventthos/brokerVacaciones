package com.tareavacaciones.brokermessagebe.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDto {
    private String orderCode;
    private String orderDate;
    private Double totalAmount;
    private String status;
    private String userId;
    private boolean fromRetry;

    // Relación anidada
    private List<ProductItem> products;
}
