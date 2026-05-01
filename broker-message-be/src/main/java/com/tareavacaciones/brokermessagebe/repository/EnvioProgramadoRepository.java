package com.tareavacaciones.brokermessagebe.repository;

import com.tareavacaciones.brokermessagebe.models.EnvioProgramado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnvioProgramadoRepository extends JpaRepository<EnvioProgramado, Long> {
    List<EnvioProgramado> findByStatusEnvio(String statusEnvio);
}
