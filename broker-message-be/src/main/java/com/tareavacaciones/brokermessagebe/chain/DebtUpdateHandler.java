package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.models.Order;
import com.tareavacaciones.brokermessagebe.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DebtUpdateHandler extends PaymentHandler {
    private final OrderRepository orderRepository;

    @Override
    public void handle(OrderPaymentKafkaDto dto) {
        orderRepository.findById(dto.getOrdenId()).ifPresent(order -> {
            double currentDebt = order.getDebt() != null ? order.getDebt() : order.getTotalAmount();
            double paymentAmount = dto.getAmount();
            double newDebt = currentDebt - paymentAmount;
            
            order.setDebt(newDebt);
            orderRepository.save(order);
            
            if (newDebt <= 0) {
                next(dto);
            }
        });
    }
}
