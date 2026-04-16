package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.CreateOrderDto;
import com.tareavacaciones.brokermessagebe.models.OrderRetryJob;
import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProcesarPagoDto;
import com.tareavacaciones.brokermessagebe.repository.OrderRetryJobRepository;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderRetryJobRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order_retry_jobs", groupId = "retry-group")
    public void listenOrders(String message) {

        log.info("🔥 MENSAJE RECIBIDO DE ORDENES: {}", message);

        if (message.contains("\"data\":")) {
            log.info("⏭️ Ignorando mensaje de actualización");
            return;
        }

        try {
            CreateOrderDto dto = objectMapper.readValue(message, CreateOrderDto.class);

            OrderRetryJob job = OrderRetryJob.builder()
                    .ordenId(dto.getOrderCode())
                    .requestData(message)
                    .action("CREATE_ORDER")
                    .step("RETRY_ORDER")
                    .status("SCHEDULED")
                    .retryCount(0)
                    .nextRunAt(OffsetDateTime.now())
                    .build();

            repository.save(job);

        } catch (Exception e) {
            log.error("Error al consumir mensaje: {}", e.getMessage());
        }
    }
}