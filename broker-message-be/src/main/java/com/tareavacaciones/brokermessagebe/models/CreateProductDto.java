package com.tareavacaciones.brokermessagebe.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProductDto {
    private String name;
    private String description;
    private Double price;
    private int quantity;
    private String imageUrl;
    private String supplier;
    private boolean fromRetry;
}
