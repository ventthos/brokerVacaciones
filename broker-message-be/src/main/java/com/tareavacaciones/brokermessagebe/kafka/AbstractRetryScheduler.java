package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
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
            try {
                retry(job);
                updateStepStatus(job, "retry", "SUCCESS", "Retry successful");
            } catch (Exception e) {
                updateStepStatus(job, "retry", "ERROR", e.getMessage());
                saveJob(job);
                throw e;
            }
            saveJob(job);

            try {
                sendSuccessEmail(job);
                updateStepStatus(job, "sendEmail", "SUCCESS", "Email sent successfully");
            } catch (Exception e) {
                updateStepStatus(job, "sendEmail", "ERROR", "Error al enviar el correo: " + e.getMessage());
                saveJob(job);
                sendFailureSendingEmail(job, e);
            }
            saveJob(job);

            updateStepStatus(job, "updateRetryJobs", "SUCCESS", "Elemento guardado correctamente");
            markAsSuccess(job);

        } catch (Exception e) {
            updateStepStatus(job, "updateRetryJobs", "ERROR", "Fallo en la operación: " + e.getMessage());
            handleFailure(job, e);
        }
    }

    protected abstract void saveJob(T job);

    protected void updateStepStatus(T job, String step, String status, String message) {
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

    protected void sendFailureSendingEmail(T job, Exception e){
        emailService.sendEmail(
                "ventthos@gmail.com",
                "Error",
                "El elemento ha sido guardado pero no se pudo mandar el email"
        );
    }
}