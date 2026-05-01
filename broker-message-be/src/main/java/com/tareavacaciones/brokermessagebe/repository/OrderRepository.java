package com.tareavacaciones.brokermessagebe.repository;

import com.tareavacaciones.brokermessagebe.models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findByOrderCode(String orderCode);
}
