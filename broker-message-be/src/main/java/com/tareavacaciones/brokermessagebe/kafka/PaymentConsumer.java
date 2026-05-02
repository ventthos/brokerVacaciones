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
    private final com.tareavacaciones.brokermessagebe.repository.PaymentDebtRetryJobRepository debtRetryRepository;
    private final ObjectMapper objectMapper;
    private final PaymentChainConfig chainConfig;

    @KafkaListener(topics = "payments_retry_jobs", groupId = "retry-group")
    public void listenPayments(String message) {
        // ... (código existente)
    }

    @KafkaListener(topics = "order_debt_payment", groupId = "payment-group")
    public void listenOrderDebtPayment(String message) {
        log.info("💰 MENSAJE DE PAGO DE DEUDA RECIBIDO: {}", message);
        try {
            OrderPaymentKafkaDto dto = objectMapper.readValue(message, OrderPaymentKafkaDto.class);
            
            com.tareavacaciones.brokermessagebe.models.PaymentDebtRetryJob job = 
                com.tareavacaciones.brokermessagebe.models.PaymentDebtRetryJob.builder()
                    .ordenId(dto.getOrdenId())
                    .requestData(message)
                    .status("SCHEDULED")
                    .retryCount(0)
                    .nextRunAt(OffsetDateTime.now())
                    .build();
            
            debtRetryRepository.save(job);
            
            boolean success = chainConfig.getFirstHandler().handle(dto, job);
            
            if (success) {
                job.setStatus("COMPLETED");
            } else {
                job.setNextRunAt(OffsetDateTime.now().plusSeconds(30));
                log.warn("Payment chain execution failed for order {}. Scheduled for retry.", dto.getOrdenId());
            }
            debtRetryRepository.save(job);
            
        } catch (Exception e) {
            log.error("Error al procesar pago de deuda: {}", e.getMessage());
        }
    }
}
