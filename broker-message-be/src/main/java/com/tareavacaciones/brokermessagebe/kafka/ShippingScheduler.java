package com.tareavacaciones.brokermessagebe.kafka;

import com.tareavacaciones.brokermessagebe.models.EnvioProgramado;
import com.tareavacaciones.brokermessagebe.repository.EnvioProgramadoRepository;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingScheduler {

    private final EnvioProgramadoRepository repository;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 10000)
    public void processPendingShipments() {
        log.info("Checking for pending shipments...");
        List<EnvioProgramado> pending = repository.findByStatusEnvio("En proceso");
        
        for (EnvioProgramado envio : pending) {
            envio.setStatusEnvio("Enviado");
            repository.save(envio);
            
            emailService.sendEmail(
                    "ventthos@gmail.com",
                    "Envío en camino",
                    "Su envío para la orden " + envio.getOrdenId() + " está en camino"
            );
            log.info("Shipment for order {} marked as Enviado and email sent.", envio.getOrdenId());
        }
    }
}
