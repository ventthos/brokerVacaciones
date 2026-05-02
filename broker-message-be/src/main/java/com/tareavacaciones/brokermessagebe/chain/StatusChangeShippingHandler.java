package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.EnvioProgramado;
import com.tareavacaciones.brokermessagebe.models.StatusChangeDto;
import com.tareavacaciones.brokermessagebe.repository.EnvioProgramadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusChangeShippingHandler extends StatusChangeHandler {
    private final EnvioProgramadoRepository envioProgramadoRepository;

    @Override
    public void handle(StatusChangeDto dto) {
        if ("Pagado".equalsIgnoreCase(dto.getNewStatus())) {
            log.info("Programando envío para la orden: {}", dto.getOrderId());
            EnvioProgramado envio = EnvioProgramado.builder()
                    .ordenId(dto.getOrderId())
                    .statusEnvio("En proceso")
                    .build();
            envioProgramadoRepository.save(envio);
        }
        next(dto);
    }
}
