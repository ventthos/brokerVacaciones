package com.tareavacaciones.brokermessagebe.repository;

import com.tareavacaciones.brokermessagebe.models.OrderRetryJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRetryJobRepository extends JpaRepository<OrderRetryJob, UUID> {
    @Query("""
    SELECT j FROM OrderRetryJob j
    WHERE j.status = 'SCHEDULED'
    AND j.nextRunAt <= :now
""")
    List<OrderRetryJob> findScheduledJobs(OffsetDateTime now);
}
