package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.models.PaymentDebtRetryJob;
import lombok.Setter;

public abstract class PaymentHandler {
    @Setter
    protected PaymentHandler next;

    public abstract boolean handle(OrderPaymentKafkaDto dto, PaymentDebtRetryJob job);

    protected boolean next(OrderPaymentKafkaDto dto, PaymentDebtRetryJob job) {
        if (next != null) {
            return next.handle(dto, job);
        }
        return true;
    }
}
