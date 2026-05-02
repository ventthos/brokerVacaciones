package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.StatusChangeDto;
import lombok.Setter;

public abstract class StatusChangeHandler {
    @Setter
    protected StatusChangeHandler next;

    public abstract void handle(StatusChangeDto dto);

    protected void next(StatusChangeDto dto) {
        if (next != null) {
            next.handle(dto);
        }
    }
}
