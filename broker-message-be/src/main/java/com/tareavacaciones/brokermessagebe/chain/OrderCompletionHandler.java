package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCompletionHandler extends PaymentHandler {
    private final OrderRepository orderRepository;

    @Override
    public void handle(OrderPaymentKafkaDto dto) {
        orderRepository.findById(dto.getOrdenId()).ifPresent(order -> {
            order.setStatus("Pagada");
            orderRepository.save(order);
        });
        next(dto);
    }
}
