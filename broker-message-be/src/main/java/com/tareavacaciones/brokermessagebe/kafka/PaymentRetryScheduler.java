package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.tareavacaciones.brokermessagebe.models.OrderRetryJob;
import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProcesarPagoDto;
import com.tareavacaciones.brokermessagebe.repository.PaymentRetryJobRepository;
import com.tareavacaciones.brokermessagebe.response.GeneralResponse;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
public class PaymentRetryScheduler
        extends AbstractRetryScheduler<PaymentRetryJob> {

    private final PaymentRetryJobRepository repository;
    private final RestTemplate restTemplate;

    public PaymentRetryScheduler(ObjectMapper objectMapper,
                                 EmailService emailService,
                                 PaymentRetryJobRepository repository,
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
    protected void retry(PaymentRetryJob job) throws Exception {

        ProcesarPagoDto dto =
                objectMapper.readValue(job.getRequestData(), ProcesarPagoDto.class);

        String url = "http://localhost:8085/pagos/procesar";

        var response = restTemplate.postForEntity(url, dto, Object.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Error en pago");
        }

        job.setStep("SEND_EMAIL");
    }

    @Override
    protected void saveJob(PaymentRetryJob job) {
        repository.save(job);
    }
}