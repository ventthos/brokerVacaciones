package com.tareavacaciones.brokermessagebe.chain;

import com.tareavacaciones.brokermessagebe.kafka.AbstractRetryScheduler;
import com.tareavacaciones.brokermessagebe.models.RetryJob;

public abstract class RetryHandler<T extends RetryJob> {
    protected RetryHandler<T> next;

    public void setNext(RetryHandler<T> next) {
        this.next = next;
    }

    public abstract void handle(T job, AbstractRetryScheduler<T> scheduler) throws Exception;

    protected void next(T job, AbstractRetryScheduler<T> scheduler) throws Exception {
        if (next != null) {
            next.handle(job, scheduler);
        }
    }
}
