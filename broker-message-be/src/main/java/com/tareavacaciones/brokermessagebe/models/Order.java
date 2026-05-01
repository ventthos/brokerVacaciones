package com.tareavacaciones.brokermessagebe.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "ordenes")
public class Order {
    @Id
    private String id;

    private String orderCode;
    private String orderDate;
    private Double totalAmount;
    private String status;
    private String user;
    private Double debt;

    // Relación anidada
    private List<ProductItem> products;
}