package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProcesarPagoDto;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentRetryJobRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payments_retry_jobs", groupId = "retry-group")
    public void listenPayments(String message) {

        log.info("🔥 MENSAJE RECIBIDO: {}", message);

        if (message.contains("\"data\":")) {
            log.info("⏭️ Ignorando mensaje de actualización");
            return;
        }

        try {
            ProcesarPagoDto dto = objectMapper.readValue(message, ProcesarPagoDto.class);

            PaymentRetryJob job = PaymentRetryJob.builder()
                    .paymentId(null)
                    .ordenId(dto.getOrdenId())
                    .requestData(message)
                    .action("CREATE_PAYMENT")
                    .step("RETRY_PAYMENT")
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