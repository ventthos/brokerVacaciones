package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.models.CreateOrderDto;
import com.tareavacaciones.brokermessagebe.models.OrderRetryJob;
import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProcesarPagoDto;
import com.tareavacaciones.brokermessagebe.repository.OrderRetryJobRepository;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class OrderRetryScheduler
        extends AbstractRetryScheduler<OrderRetryJob> {

    private final OrderRetryJobRepository repository;
    private final RestTemplate restTemplate;

    public OrderRetryScheduler(ObjectMapper objectMapper,
                               EmailService emailService,
                               OrderRetryJobRepository repository,
                               RestTemplate restTemplate) {
        super(objectMapper, emailService);
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 10000)
    public void processPayments() {
        var jobs = repository.findScheduledJobs(OffsetDateTime.now());
        processJobs(jobs);
    }

    @Override
    protected void retry(OrderRetryJob job) throws Exception {

        CreateOrderDto dto =
                objectMapper.readValue(job.getRequestData(), CreateOrderDto.class);

        String url = "http://localhost:8085/ordenes";

        var response = restTemplate.postForEntity(url, dto, Object.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Error en orden");
        }

        job.setStep("SEND_EMAIL");
    }

    @Override
    protected void saveJob(OrderRetryJob job) {
        repository.save(job);
    }
}