package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.tareavacaciones.brokermessagebe.chain.JobCompletionHandler;
import com.tareavacaciones.brokermessagebe.chain.JobEmailHandler;
import com.tareavacaciones.brokermessagebe.chain.JobExecutionHandler;
import com.tareavacaciones.brokermessagebe.chain.RetryHandler;
import com.tareavacaciones.brokermessagebe.models.RetryJob;
import com.tareavacaciones.brokermessagebe.models.StepResult;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractRetryScheduler<T extends RetryJob> {

    protected final ObjectMapper objectMapper;
    protected final EmailService emailService;
    protected final RetryHandler<T> firstHandler;

    public AbstractRetryScheduler(ObjectMapper objectMapper,
                                  EmailService emailService) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
        this.firstHandler = buildChain();
    }

    protected RetryHandler<T> buildChain() {
        JobExecutionHandler<T> executionHandler = new JobExecutionHandler<>();
        JobEmailHandler<T> emailHandler = new JobEmailHandler<>();
        JobCompletionHandler<T> completionHandler = new JobCompletionHandler<>();

        executionHandler.setNext(emailHandler);
        emailHandler.setNext(completionHandler);

        return executionHandler;
    }

    public void processJobs(List<T> jobs) {
        for (T job : jobs) {
            processJob(job);
        }
    }

    private void processJob(T job) {
        try {
            firstHandler.handle(job, this);
        } catch (Exception e) {
            handleFailure(job, e);
        }
    }

    public abstract void saveJob(T job);

    public boolean isStepSuccess(T job, String step) {
        try {
            if (job.getStepStatus() == null || job.getStepStatus().isEmpty()) {
                return false;
            }
            Map<String, StepResult> statusMap = objectMapper.readValue(
                    job.getStepStatus(),
                    new TypeReference<Map<String, StepResult>>() {}
            );
            StepResult result = statusMap.get(step);
            return result != null && "SUCCESS".equals(result.getStatus());
        } catch (Exception e) {
            log.error("Error checking step status", e);
            return false;
        }
    }

    public void updateStepStatus(T job, String step, String status, String message) {
        try {
            Map<String, StepResult> statusMap;
            if (job.getStepStatus() == null || job.getStepStatus().isEmpty()) {
                statusMap = new HashMap<>();
            } else {
                statusMap = objectMapper.readValue(job.getStepStatus(), new TypeReference<Map<String, StepResult>>() {});
            }

            statusMap.put(step, new StepResult(status, message));
            job.setStepStatus(objectMapper.writeValueAsString(statusMap));
        } catch (Exception e) {
            log.error("Error updating step status", e);
        }
    }

    public abstract void retry(T job) throws Exception;

    public void markAsSuccess(T job) {
        job.setStatus("SUCCESS");
        saveJob(job);
    }

    protected void handleFailure(T job, Exception e) {
        job.setRetryCount(job.getRetryCount() + 1);
        job.setErrorMessage(e.getMessage());

        if (job.getRetryCount() >= 2) {
            job.setStatus("FAILED");
            sendFailureEmail();
        } else {
            job.setStatus("SCHEDULED");
            job.setNextRunAt(java.time.OffsetDateTime.now().plusSeconds(10));
        }
        saveJob(job);
    }

    public void sendSuccessEmail(T job) throws Exception {
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

    protected void sendFailureSendingEmail(T job, Exception e){
        emailService.sendEmail(
                "ventthos@gmail.com",
                "Error",
                "El elemento ha sido guardado pero no se pudo mandar el email"
        );
    }
}