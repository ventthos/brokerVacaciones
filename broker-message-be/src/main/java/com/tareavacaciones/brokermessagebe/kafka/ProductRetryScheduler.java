package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.CreateProductDto;
import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProcesarPagoDto;
import com.tareavacaciones.brokermessagebe.models.ProductRetryJob;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import com.tareavacaciones.brokermessagebe.repository.ProductRetryJobRepository;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;

import org.springframework.kafka.core.KafkaTemplate;

@Service
@Slf4j
public class ProductRetryScheduler
        extends AbstractRetryScheduler<ProductRetryJob> {

    private final ProductRetryJobRepository repository;
    private final RestTemplate restTemplate;

    public ProductRetryScheduler(ObjectMapper objectMapper,
                                 EmailService emailService,
                                 KafkaTemplate<String, String> kafkaTemplate,
                                 ProductRetryJobRepository repository,
                                 RestTemplate restTemplate) {
        super(objectMapper, emailService, kafkaTemplate);
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 10000)
    public void processPayments() {
        var jobs = repository.findScheduledJobs(OffsetDateTime.now());
        processJobs(jobs);
    }

    @Override
    protected String getRequestData(ProductRetryJob job) {
        return job.getRequestData();
    }

    @Override
    protected String getUpdateTopic() {
        return "product_retry_jobs";
    }

    @Override
    protected String getJobId(ProductRetryJob job) {
        return job.getId().toString();
    }

    @Override
    protected boolean isFinalFailure(ProductRetryJob job) {
        return "FAILED".equals(job.getStatus());
    }

    @Override
    protected void retry(ProductRetryJob job) throws Exception {

        CreateProductDto dto =
                objectMapper.readValue(job.getRequestData(), CreateProductDto.class);

        String url = "http://localhost:8085/productos";

        var response = restTemplate.postForEntity(url, dto, Object.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Error en producto");
        }

        job.setStep("SEND_EMAIL");
    }

    @Override
    protected void markAsSuccess(ProductRetryJob job) {
        job.setStatus("SUCCESS");
        repository.save(job);
    }

    @Override
    protected void handleFailure(ProductRetryJob job, Exception e) {

        job.setRetryCount(job.getRetryCount() + 1);
        job.setErrorMessage(e.getMessage());

        if (job.getRetryCount() >= 2) {
            job.setStatus("FAILED");
            sendFailureEmail();
        } else {
            job.setStatus("SCHEDULED");
            job.setNextRunAt(OffsetDateTime.now().plusSeconds(10));
        }

        repository.save(job);
    }

    @Override
    protected void sendFailureSendingEmail(ProductRetryJob job, Exception e) {
        super.sendFailureSendingEmail(job, e);

        job.setErrorMessage("Orden completada, pero falló el envío de confirmación: " + e.getMessage());
        job.setStatus("ERROR");
        repository.save(job);

    }
}