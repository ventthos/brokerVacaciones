package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.*;
import com.tareavacaciones.brokermessagebe.repository.OrderRetryJobRepository;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import com.tareavacaciones.brokermessagebe.repository.ProductoRepository;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {
    protected final EmailService emailService;
    private final OrderRetryJobRepository repository;
    private final ObjectMapper objectMapper;
    private final ProductoRepository productoRepository;

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

    @KafkaListener(topics = "order_status_change")
    public void listenChangeInStatus(String message){
        log.info("Se detectó un cambio en el status de una orden");

        try {
            StatusChangeDto dto = objectMapper.readValue(message, StatusChangeDto.class);
            emailService.sendEmail(
                    "ventthos@gmail.com",
                    "Cambio en el estado de la órden",
                    "La orden con folio %s ha cambiado su estado de %s a %s".formatted(dto.getOrderId(), dto.getOldStatus(), dto.getNewStatus())
            );
        }
        catch (Exception e){
            log.error("Error al consumir mensaje de status: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "inventory_change")
    public void listenChangesInInventory(String message){
        log.info("Se detectó un intento de cambio en los inventarios");
        try{
            InventoryChangeDto dt = objectMapper.readValue(message, InventoryChangeDto.class);

            Map<String, Integer> oldMap = dt.getOldProducts().stream()
                    .collect(Collectors.toMap(
                            ProductItem::getProductId,
                            ProductItem::getQuantity
                    ));

            Map<String, Integer> newMap = dt.getNewProducts().stream()
                    .collect(Collectors.toMap(
                            ProductItem::getProductId,
                            ProductItem::getQuantity
                    ));

            Set<String> allProductIds = new HashSet<>();
            allProductIds.addAll(oldMap.keySet());
            allProductIds.addAll(newMap.keySet());

            for (String productId : allProductIds) {

                int oldQty = oldMap.getOrDefault(productId, 0);
                int newQty = newMap.getOrDefault(productId, 0);

                int diference = oldQty - newQty;

                if (diference != 0) {
                    Producto producto = productoRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                    producto.setQuantity(producto.getQuantity() + diference);

                    productoRepository.save(producto);

                    log.info("Producto {} actualizado. Cambio: {}", productId, diference);
                }
            }
        }
        catch (Exception e) {
            log.error("Error al consumir mensaje de inventario: {}", e.getMessage());
        }

    }
}