package com.tareavacaciones.brokermessagebe.repository;

import com.tareavacaciones.brokermessagebe.models.Pago;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PagoRepository extends MongoRepository<Pago, String> {
    List<Pago> findByOrdenId(String ordenId);
}