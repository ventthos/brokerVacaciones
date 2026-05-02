package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.InventoryChangeDto;
import com.tareavacaciones.brokermessagebe.models.ProductItem;
import com.tareavacaciones.brokermessagebe.models.Producto;
import com.tareavacaciones.brokermessagebe.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryUpdateHandler extends InventoryChangeHandler {

    private final ProductoRepository productoRepository;

    @Override
    public void handle(InventoryChangeDto dto) {
        log.info("Procesando cambios en el inventario para la orden: {}", dto.getOrderId());

        Map<String, Integer> oldMap = dto.getOldProducts().stream()
                .collect(Collectors.toMap(
                        ProductItem::getProductId,
                        ProductItem::getQuantity
                ));

        Map<String, Integer> newMap = dto.getNewProducts().stream()
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

            int difference = oldQty - newQty;

            if (difference != 0) {
                Producto producto = productoRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

                producto.setQuantity(producto.getQuantity() + difference);
                productoRepository.save(producto);

                log.info("Producto {} actualizado. Cambio: {}", productId, difference);
            }
        }
        next(dto);
    }
}
