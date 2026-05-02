package com.tareavacaciones.brokermessagebe.chain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.models.Order;
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
public class DebtUpdateHandler extends PaymentHandler {
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean handle(OrderPaymentKafkaDto dto, PaymentDebtRetryJob job) {
        if (isStepCompleted(job, "DEBT_UPDATE")) {
            return next(dto, job);
        }

        try {
            Order order = orderRepository.findById(dto.getOrdenId())
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + dto.getOrdenId()));

            double currentDebt = order.getDebt() != null ? order.getDebt() : order.getTotalAmount();
            double paymentAmount = dto.getAmount();
            double newDebt = Math.max(0, currentDebt - paymentAmount);

            order.setDebt(newDebt);
            orderRepository.save(order);

            updateStepStatus(job, "DEBT_UPDATE", "SUCCESS");

            if (newDebt <= 0) {
                return next(dto, job);
            }
            return true; // Terminó pero con éxito (deuda parcial)
        } catch (Exception e) {
            log.error("Error in DebtUpdateHandler: {}", e.getMessage());
            updateStepStatus(job, "DEBT_UPDATE", "FAILED");
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
