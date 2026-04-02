package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractRetryScheduler<T> {

    protected final ObjectMapper objectMapper;
    protected final EmailService emailService;

    public AbstractRetryScheduler(ObjectMapper objectMapper,
                                  EmailService emailService) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    public void processJobs(List<T> jobs) {
        for (T job : jobs) {
            processJob(job);
        }
    }

    private void processJob(T job) {
        try {
            retry(job);          // PASO A (abstracto)
            sendSuccessEmail(job); // PASO B
            markAsSuccess(job); // PASO C
        } catch (Exception e) {
            handleFailure(job, e);
        }
    }

    protected abstract void retry(T job) throws Exception;

    protected abstract void markAsSuccess(T job);

    protected abstract void handleFailure(T job, Exception e);

    protected void sendSuccessEmail(T job) throws Exception {
        emailService.sendEmail(
                "ventthos@gmail.com",
                "Operación exitosa",
                "Se completó correctamente la operación hecha"
        );
    }

    protected void sendFailureEmail() {
        emailService.sendEmail(
                "ventthos@gmail.com",
                "Error",
                "Falló después de varios intentos la operación solicitada"
        );
    }
}