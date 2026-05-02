package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.kafka.AbstractRetryScheduler;
import com.tareavacaciones.brokermessagebe.models.RetryJob;

public class JobEmailHandler<T extends RetryJob> extends RetryHandler<T> {
    @Override
    public void handle(T job, AbstractRetryScheduler<T> scheduler) throws Exception {
        if (!scheduler.isStepSuccess(job, "sendEmail")) {
            try {
                scheduler.sendSuccessEmail(job);
                scheduler.updateStepStatus(job, "sendEmail", "SUCCESS", "Email enviado");
                scheduler.saveJob(job);
            } catch (Exception e) {
                scheduler.updateStepStatus(job, "sendEmail", "ERROR", "Error al enviar el correo: " + e.getMessage());
                scheduler.saveJob(job);
                throw e;
            }
        }
        next(job, scheduler);
    }
}
