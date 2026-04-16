package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.CreateProductDto;
import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProcesarPagoDto;
import com.tareavacaciones.brokermessagebe.models.ProductRetryJob;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import com.tareavacaciones.brokermessagebe.repository.ProductRetryJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductConsumer {

    private final ProductRetryJobRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "product_retry_jobs", groupId = "retry-group")
    public void listenPayments(String message) {

        log.info("🔥 MENSAJE RECIBIDO EN PRODUCTO: {}", message);

        if (message.contains("\"data\":")) {
            log.info("⏭️ Ignorando mensaje de actualización");
            return;
        }

        try {
            CreateProductDto dto = objectMapper.readValue(message, CreateProductDto.class);

            ProductRetryJob job = ProductRetryJob.builder()
                    .name(dto.getName())
                    .requestData(message)
                    .action("CREATE_PRODUCT")
                    .step("RETRY_PRODUCT")
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