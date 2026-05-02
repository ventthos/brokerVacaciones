package com.tareavacaciones.brokermessagebe.repository;

import com.tareavacaciones.brokermessagebe.models.PaymentDebtRetryJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentDebtRetryJobRepository extends JpaRepository<PaymentDebtRetryJob, UUID> {
    @Query("SELECT j FROM PaymentDebtRetryJob j WHERE j.status = 'SCHEDULED' AND j.nextRunAt <= :now")
    List<PaymentDebtRetryJob> findScheduledJobs(@Param("now") OffsetDateTime now);
}
