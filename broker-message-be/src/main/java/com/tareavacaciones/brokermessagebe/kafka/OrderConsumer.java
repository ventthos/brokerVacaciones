package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.configuration.InventoryChangeChainConfig;
import com.tareavacaciones.brokermessagebe.configuration.StatusChangeChainConfig;
import com.tareavacaciones.brokermessagebe.models.*;
import com.tareavacaciones.brokermessagebe.repository.OrderRetryJobRepository;
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
    private final StatusChangeChainConfig statusChangeChainConfig;
    private final InventoryChangeChainConfig inventoryChangeChainConfig;

    @KafkaListener(topics = "order_retry_jobs", groupId = "retry-group")
    public void listenOrders(String message) {

        log.info("🔥 MENSAJE RECIBIDO DE ORDENES: {}", message);

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

    @KafkaListener(topics = "status_update_events")
    public void listenChangeInStatus(String message){
        log.info("Se detectó un cambio en el status de una orden");

        try {
            StatusChangeDto dto = objectMapper.readValue(message, StatusChangeDto.class);
            statusChangeChainConfig.getFirstHandler().handle(dto);
        }
        catch (Exception e){
            log.error("Error al consumir mensaje de status: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "inventory_update_events")
    public void listenChangesInInventory(String message){
        log.info("Se detectó un intento de cambio en los inventarios");
        try{
            InventoryChangeDto dt = objectMapper.readValue(message, InventoryChangeDto.class);
            inventoryChangeChainConfig.getFirstHandler().handle(dt);
        }
        catch (Exception e) {
            log.error("Error al consumir mensaje de inventario: {}", e.getMessage());
        }

    }
}