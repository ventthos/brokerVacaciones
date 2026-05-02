package com.tareavacaciones.brokermessagebe.chain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.models.PaymentDebtRetryJob;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationHandler extends PaymentHandler {
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean handle(OrderPaymentKafkaDto dto, PaymentDebtRetryJob job) {
        if (isStepCompleted(job, "EMAIL_NOTIFICATION")) {
            return next(dto, job);
        }

        try {
            emailService.sendEmail(
                    "ventthos@gmail.com",
                    "Notificación de Pago Recibido",
                    "Se ha recibido un pago para la orden ID: " + dto.getOrdenId() + " por un monto de: " + dto.getAmount()
            );
            updateStepStatus(job, "EMAIL_NOTIFICATION", "SUCCESS");
            return next(dto, job);
        } catch (Exception e) {
            log.error("Error in EmailNotificationHandler: {}", e.getMessage());
            updateStepStatus(job, "EMAIL_NOTIFICATION", "FAILED");
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
