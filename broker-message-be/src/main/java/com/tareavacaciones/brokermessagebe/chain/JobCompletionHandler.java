package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.kafka.AbstractRetryScheduler;
import com.tareavacaciones.brokermessagebe.models.RetryJob;

public class JobCompletionHandler<T extends RetryJob> extends RetryHandler<T> {
    @Override
    public void handle(T job, AbstractRetryScheduler<T> scheduler) throws Exception {
        if (!scheduler.isStepSuccess(job, "updateRetryJobs")) {
            try {
                scheduler.updateStepStatus(job, "updateRetryJobs", "SUCCESS", "Elemento guardado correctamente");
                scheduler.markAsSuccess(job);
            } catch (Exception e) {
                scheduler.updateStepStatus(job, "updateRetryJobs", "ERROR", "Error al actualizar estado final: " + e.getMessage());
                scheduler.saveJob(job);
                throw e;
            }
        }
        next(job, scheduler);
    }
}
