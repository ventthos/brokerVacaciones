package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.models.StatusChangeDto;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatusChangeEmailHandler extends StatusChangeHandler {
    private final EmailService emailService;

    @Override
    public void handle(StatusChangeDto dto) {
        emailService.sendEmail(
                "ventthos@gmail.com",
                "Cambio en el estado de la órden",
                "La orden con folio %s ha cambiado su estado de %s a %s".formatted(dto.getOrderId(), dto.getOldStatus(), dto.getNewStatus())
        );
        next(dto);
    }
}
