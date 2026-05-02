package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.InventoryChangeDto;
import lombok.Setter;

public abstract class InventoryChangeHandler {
    @Setter
    protected InventoryChangeHandler next;

    public abstract void handle(InventoryChangeDto dto);

    protected void next(InventoryChangeDto dto) {
        if (next != null) {
            next.handle(dto);
        }
    }
}
