package com.tareavacaciones.brokermessagebe.repository;

import com.tareavacaciones.brokermessagebe.models.PaymentRetryJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentRetryJobRepository extends JpaRepository<PaymentRetryJob, UUID> {
    @Query("""
    SELECT j FROM PaymentRetryJob j
    WHERE j.status = 'SCHEDULED'
    AND j.nextRunAt <= :now
""")
    List<PaymentRetryJob> findScheduledJobs(OffsetDateTime now);
}
