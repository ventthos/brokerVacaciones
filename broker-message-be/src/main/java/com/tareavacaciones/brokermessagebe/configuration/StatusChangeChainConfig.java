package com.tareavacaciones.brokermessagebe.configuration;

import com.tareavacaciones.brokermessagebe.chain.StatusChangeEmailHandler;
import com.tareavacaciones.brokermessagebe.chain.StatusChangeHandler;
import com.tareavacaciones.brokermessagebe.chain.StatusChangeShippingHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StatusChangeChainConfig {

    private final StatusChangeEmailHandler emailHandler;
    private final StatusChangeShippingHandler shippingHandler;

    @PostConstruct
    public void configureChain() {
        emailHandler.setNext(shippingHandler);
    }

    public StatusChangeHandler getFirstHandler() {
        return emailHandler;
    }
}
