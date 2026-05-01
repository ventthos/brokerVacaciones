package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailNotificationHandler extends PaymentHandler {
    private final EmailService emailService;

    @Override
    public void handle(OrderPaymentKafkaDto dto) {
        emailService.sendEmail(
                "ventthos@gmail.com",
                "Notificación de Pago Recibido",
                "Se ha recibido un pago para la orden ID: " + dto.getOrdenId() + " por un monto de: " + dto.getAmount()
        );
        next(dto);
    }
}
