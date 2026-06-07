package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.configuration.StatusChangeChainConfig;
import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.repository.OrderRepository;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCompletionHandler extends PaymentHandler {
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Override
    public void handle(OrderPaymentKafkaDto dto) {
        orderRepository.findById(dto.getOrdenId()).ifPresent(order -> {
            order.setStatus("Pagado");
            orderRepository.save(order);

            emailService.sendEmail(
                    "ventthos@gmail.com",
                    "Cambio en el estado de la órden",
                    "La orden con folio %s ha cambiado su estado a Pagado".formatted(dto.getOrdenId())
            );
        });
        next(dto);
    }
}
