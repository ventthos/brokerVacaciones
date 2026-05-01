package com.tareavacaciones.brokermessagebe.configuration;

import com.tareavacaciones.brokermessagebe.chain.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PaymentChainConfig {

    private final EmailNotificationHandler emailNotificationHandler;
    private final DebtUpdateHandler debtUpdateHandler;
    private final OrderCompletionHandler orderCompletionHandler;
    private final ShippingSchedulingHandler shippingSchedulingHandler;

    @PostConstruct
    public void configureChain() {
        emailNotificationHandler.setNext(debtUpdateHandler);
        debtUpdateHandler.setNext(orderCompletionHandler);
        orderCompletionHandler.setNext(shippingSchedulingHandler);
    }

    public PaymentHandler getFirstHandler() {
        return emailNotificationHandler;
    }
}
