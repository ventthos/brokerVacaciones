package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import lombok.Setter;

public abstract class PaymentHandler {
    @Setter
    protected PaymentHandler next;

    public abstract void handle(OrderPaymentKafkaDto dto);

    protected void next(OrderPaymentKafkaDto dto) {
        if (next != null) {
            next.handle(dto);
        }
    }
}
