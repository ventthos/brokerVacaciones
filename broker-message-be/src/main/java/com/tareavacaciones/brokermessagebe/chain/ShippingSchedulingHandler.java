package com.tareavacaciones.brokermessagebe.chain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.EnvioProgramado;
import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.models.PaymentDebtRetryJob;
import com.tareavacaciones.brokermessagebe.repository.EnvioProgramadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingSchedulingHandler extends PaymentHandler {
    private final EnvioProgramadoRepository envioProgramadoRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean handle(OrderPaymentKafkaDto dto, PaymentDebtRetryJob job) {
        if (isStepCompleted(job, "SHIPPING_SCHEDULING")) {
            return next(dto, job);
        }

        try {
            EnvioProgramado envio = EnvioProgramado.builder()
                    .ordenId(dto.getOrdenId())
                    .statusEnvio("En proceso")
                    .build();
            envioProgramadoRepository.save(envio);
            updateStepStatus(job, "SHIPPING_SCHEDULING", "SUCCESS");
            return next(dto, job);
        } catch (Exception e) {
            log.error("Error in ShippingSchedulingHandler: {}", e.getMessage());
            updateStepStatus(job, "SHIPPING_SCHEDULING", "FAILED");
            return false;
        }
    }

    private boolean isStepCompleted(PaymentDebtRetryJob job, String step) {
        try {
            if (job.getStepStatus() == null) return false;
            Map<String, String> statusMap = objectMapper.readValue(job.getStepStatus(), new TypeReference<>() {});
            return "SUCCESS".equals(statusMap.get(step));
        } catch (Exception e) {
            return false;
        }
    }

    private void updateStepStatus(PaymentDebtRetryJob job, String step, String status) {
        try {
            Map<String, String> statusMap = job.getStepStatus() == null ? new HashMap<>() : 
                objectMapper.readValue(job.getStepStatus(), new TypeReference<>() {});
            statusMap.put(step, status);
            job.setStepStatus(objectMapper.writeValueAsString(statusMap));
        } catch (Exception ignored) {}
    }
}
