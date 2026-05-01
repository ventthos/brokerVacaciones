package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.EnvioProgramado;
import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.repository.EnvioProgramadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShippingSchedulingHandler extends PaymentHandler {
    private final EnvioProgramadoRepository envioProgramadoRepository;

    @Override
    public void handle(OrderPaymentKafkaDto dto) {
        EnvioProgramado envio = EnvioProgramado.builder()
                .ordenId(dto.getOrdenId())
                .statusEnvio("En proceso")
                .build();
        envioProgramadoRepository.save(envio);
        next(dto);
    }
}
