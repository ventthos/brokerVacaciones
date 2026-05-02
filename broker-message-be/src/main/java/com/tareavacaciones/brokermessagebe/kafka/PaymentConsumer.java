package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProcesarPagoDto;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import com.tareavacaciones.brokermessagebe.configuration.PaymentChainConfig;
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
    private final PaymentChainConfig chainConfig;

    @KafkaListener(topics = "payments_retry_jobs", groupId = "retry-group")
    public void listenPayments(String message) {

        log.info("🔥 MENSAJE RECIBIDO: {}", message);

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

    @KafkaListener(topics = "payment_received_events", groupId = "payment-group")
    public void listenOrderDebtPayment(String message) {
        log.info("💰 MENSAJE DE PAGO DE DEUDA RECIBIDO: {}", message);
        try {
            OrderPaymentKafkaDto dto = objectMapper.readValue(message, OrderPaymentKafkaDto.class);
            chainConfig.getFirstHandler().handle(dto);
        } catch (Exception e) {
            log.error("Error al procesar pago de deuda: {}", e.getMessage());
        }
    }
}
