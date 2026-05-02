package com.tareavacaciones.brokermessagebe.chain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.models.PaymentDebtRetryJob;
import com.tareavacaciones.brokermessagebe.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCompletionHandler extends PaymentHandler {
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean handle(OrderPaymentKafkaDto dto, PaymentDebtRetryJob job) {
        if (isStepCompleted(job, "ORDER_COMPLETION")) {
            return next(dto, job);
        }

        try {
            orderRepository.findById(dto.getOrdenId()).ifPresent(order -> {
                order.setStatus("Pagada");
                orderRepository.save(order);
            });
            updateStepStatus(job, "ORDER_COMPLETION", "SUCCESS");
            return next(dto, job);
        } catch (Exception e) {
            log.error("Error in OrderCompletionHandler: {}", e.getMessage());
            updateStepStatus(job, "ORDER_COMPLETION", "FAILED");
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
