package com.tareavacaciones.brokermessagebe.configuration;

import com.tareavacaciones.brokermessagebe.chain.InventoryChangeHandler;
import com.tareavacaciones.brokermessagebe.chain.InventoryUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InventoryChangeChainConfig {

    private final InventoryUpdateHandler inventoryUpdateHandler;

    public InventoryChangeHandler getFirstHandler() {
        return inventoryUpdateHandler;
    }
}
