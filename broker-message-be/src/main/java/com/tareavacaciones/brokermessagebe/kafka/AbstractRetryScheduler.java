package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.MessageWrapper;
import com.tareavacaciones.brokermessagebe.models.StepStatus;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractRetryScheduler<T> {

    protected final ObjectMapper objectMapper;
    protected final EmailService emailService;
    protected final KafkaTemplate<String, String> kafkaTemplate;

    public AbstractRetryScheduler(ObjectMapper objectMapper,
                                  EmailService emailService,
                                  KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processJobs(List<T> jobs) {
        for (T job : jobs) {
            processJob(job);
        }
    }

    private void processJob(T job) {
        MessageWrapper wrapper = createInitialWrapper(job);
        try {
            // PASO 1: RETRY (Intento de enviar al endpoint)
            try {
                retry(job);
                updateDataStatus(wrapper, "SUCCESS");
                publishUpdate(job, wrapper);
            } catch (Exception e) {
                updateDataStatus(wrapper, "FAILED");
                publishUpdate(job, wrapper);
                throw e; // Rethrow to trigger handleFailure (DB/reschedule logic)
            }

            // PASO 2: EMAIL
            try {
                sendSuccessEmail(job);
                updateEmailStatus(wrapper, "SUCCESS", "Si se pudo enviar el mensaje");
            } catch (Exception e) {
                sendFailureSendingEmail(job, e);
                updateEmailStatus(wrapper, "FAILED", "Error al enviar el correo");
            }
            publishUpdate(job, wrapper);

            // PASO 3: DB UPDATE (markAsSuccess)
            markAsSuccess(job);
            updateRetryJobsStatus(wrapper, "SUCCESS", "Pago creado correctamente");
            publishUpdate(job, wrapper);

        } catch (Exception e) {
            handleFailure(job, e);
            // Si el job terminó en FAILED definitivo, publicamos el estado final en DB
            if (isFinalFailure(job)) {
                updateRetryJobsStatus(wrapper, "FAILED", "Se agotaron los reintentos");
                publishUpdate(job, wrapper);
            }
        }
    }

    protected abstract void retry(T job) throws Exception;

    protected abstract void markAsSuccess(T job);

    protected abstract void handleFailure(T job, Exception e);

    protected abstract String getRequestData(T job);

    protected abstract String getUpdateTopic();

    protected abstract String getJobId(T job);

    protected abstract boolean isFinalFailure(T job);

    private MessageWrapper createInitialWrapper(T job) {
        try {
            String requestData = getRequestData(job);
            JsonNode node = objectMapper.readTree(requestData);
            MessageWrapper wrapper;

            if (node.has("data")) {
                wrapper = objectMapper.readValue(requestData, MessageWrapper.class);
            } else {
                Map<String, Object> data = objectMapper.convertValue(node, Map.class);
                wrapper = MessageWrapper.builder().data(data).build();
            }

            // Asegurar que el ID del job esté en la data
            if (wrapper.getData() != null && !wrapper.getData().containsKey("id")) {
                wrapper.getData().put("id", getJobId(job));
            }

            return wrapper;
        } catch (Exception e) {
            log.error("Error creating initial wrapper: {}", e.getMessage());
            return MessageWrapper.builder().data(new HashMap<>()).build();
        }
    }

    private void updateDataStatus(MessageWrapper wrapper, String status) {
        if (wrapper.getData() != null) {
            wrapper.getData().put("status", status);
        }
    }

    private void updateEmailStatus(MessageWrapper wrapper, String status, String message) {
        wrapper.setSendEmail(StepStatus.builder()
                .status(status)
                .message(message)
                .build());
    }

    private void updateRetryJobsStatus(MessageWrapper wrapper, String status, String message) {
        wrapper.setUpdateRetryJobs(StepStatus.builder()
                .status(status)
                .message(message)
                .build());
    }

    private void publishUpdate(T job, MessageWrapper wrapper) {
        try {
            String message = objectMapper.writeValueAsString(wrapper);
            String topic = getUpdateTopic();
            String key = getJobId(job);
            kafkaTemplate.send(topic, key, message);
            log.info("📢 MENSAJE ACTUALIZADO EN BROKER (TOPIC: {}, KEY: {}): {}", topic, key, message);
        } catch (Exception e) {
            log.error("Error publishing update to Kafka: {}", e.getMessage());
        }
    }

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