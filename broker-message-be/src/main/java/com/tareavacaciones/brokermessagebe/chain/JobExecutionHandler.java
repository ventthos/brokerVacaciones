package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.kafka.AbstractRetryScheduler;
import com.tareavacaciones.brokermessagebe.models.RetryJob;

public class JobExecutionHandler<T extends RetryJob> extends RetryHandler<T> {
    @Override
    public void handle(T job, AbstractRetryScheduler<T> scheduler) throws Exception {
        if (!scheduler.isStepSuccess(job, "retry")) {
            try {
                scheduler.retry(job);
                scheduler.updateStepStatus(job, "retry", "SUCCESS", "Elemento guardado");
                scheduler.saveJob(job);
            } catch (Exception e) {
                scheduler.updateStepStatus(job, "retry", "ERROR", e.getMessage());
                scheduler.saveJob(job);
                throw e;
            }
        }
        next(job, scheduler);
    }
}
