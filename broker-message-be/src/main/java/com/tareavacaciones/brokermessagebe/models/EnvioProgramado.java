package com.tareavacaciones.brokermessagebe.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "envios_programados")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnvioProgramado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ordenId;
    private String statusEnvio;
}
