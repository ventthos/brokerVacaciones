package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProcesarPagoDto;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import com.tareavacaciones.brokermessagebe.response.GeneralResponse;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRetryScheduler {

    private final PaymentRetryJobRepository repository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final EmailService emailService;


    @Scheduled(fixedRate = 10000)
    public void processPayments() {

        List<PaymentRetryJob> jobs =
                repository.findScheduledJobs(OffsetDateTime.now());

        for (PaymentRetryJob job : jobs) {
            processJob(job);
        }
    }

    private void processJob(PaymentRetryJob job) {

        try {
            retryPayment(job);       // PASO A
            sendSuccessEmail(job);  // PASO B
            markAsSuccess(job);     // PASO C

        } catch (Exception e) {
            handleFailure(job, e);
        }
    }

    private void retryPayment(PaymentRetryJob job) throws Exception {

        ProcesarPagoDto dto =
                objectMapper.readValue(job.getRequestData(), ProcesarPagoDto.class);

        log.info("Reintentando pago para orden: {}", dto.getOrdenId());

        String url = "http://localhost:8085/pagos/procesar";

        try {
            ResponseEntity<GeneralResponse> response =
                    restTemplate.postForEntity(url, dto, GeneralResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new Exception("Error al crear pago");
            }

            job.setStep("SEND_EMAIL");

        } catch (Exception e) {
            throw new Exception("Fallo en endpoint de pago: " + e.getMessage());
        }
    }

    private void sendSuccessEmail(PaymentRetryJob job) throws Exception {

        try {
            emailService.sendEmail(
                    "ventthos@gmail.com",
                    "Pago exitoso",
                    "Tu pago fue procesado correctamente"
            );

            job.setStep("UPDATE_STATUS");

        } catch (Exception e) {
            throw new Exception("Error enviando correo: " + e.getMessage());
        }
    }

    private void markAsSuccess(PaymentRetryJob job) {

        job.setStatus("SUCCESS");
        repository.save(job);
    }

    private void sendFailureEmail(PaymentRetryJob job) {

        try {
            emailService.sendEmail(
                    "ventthos@gmail.com",
                    "Error en tu pago",
                    "Hubo un problema procesando tu pago después de varios intentos"
            );

        } catch (Exception e) {
            log.error("Error enviando correo de fallo: {}", e.getMessage());
        }
    }

    private void handleFailure(PaymentRetryJob job, Exception e) {

        log.error("Error en retry: {}", e.getMessage());

        job.setRetryCount(job.getRetryCount() + 1);
        job.setErrorMessage(e.getMessage());

        if (job.getRetryCount() >= 5) {
            job.setStatus("FAILED");
            sendFailureEmail(job);
        } else {
            job.setStatus("SCHEDULED");
            job.setNextRunAt(OffsetDateTime.now().plusSeconds(10));
        }

        repository.save(job);
    }
}