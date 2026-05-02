package com.tareavacaciones.brokermessagebe.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tareavacaciones.brokermessagebe.configuration.PaymentChainConfig;
import com.tareavacaciones.brokermessagebe.models.OrderPaymentKafkaDto;
import com.tareavacaciones.brokermessagebe.models.PaymentDebtRetryJob;
import com.tareavacaciones.brokermessagebe.repository.PaymentDebtRetryJobRepository;
import com.tareavacaciones.brokermessagebe.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class PaymentDebtRetryScheduler extends AbstractRetryScheduler<PaymentDebtRetryJob> {

    private final PaymentDebtRetryJobRepository repository;
    private final PaymentChainConfig chainConfig;

    public PaymentDebtRetryScheduler(ObjectMapper objectMapper,
                                     EmailService emailService,
                                     PaymentDebtRetryJobRepository repository,
                                     PaymentChainConfig chainConfig) {
        super(objectMapper, emailService);
        this.repository = repository;
        this.chainConfig = chainConfig;
    }

    @Scheduled(fixedRate = 10000)
    public void processRetries() {
        var jobs = repository.findScheduledJobs(OffsetDateTime.now());
        processJobs(jobs);
    }

    @Override
    protected void retry(PaymentDebtRetryJob job) throws Exception {
        OrderPaymentKafkaDto dto = objectMapper.readValue(job.getRequestData(), OrderPaymentKafkaDto.class);
        
        boolean success = chainConfig.getFirstHandler().handle(dto, job);
        
        if (!success) {
            throw new Exception("Some steps in the payment chain failed. Remaining attempts: " + (2 - job.getRetryCount()));
        }
        
        job.setStatus("COMPLETED");
    }

    @Override
    protected void saveJob(PaymentDebtRetryJob job) {
        repository.save(job);
    }
}
