package com.tareavacaciones.brokermessagebe.repository;

import com.tareavacaciones.brokermessagebe.models.OrderRetryJob;
import com.tareavacaciones.brokermessagebe.models.ProductRetryJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductRetryJobRepository extends JpaRepository<ProductRetryJob, UUID> {
    @Query("""
    SELECT j FROM ProductRetryJob j
    WHERE j.status = 'SCHEDULED'
    AND j.nextRunAt <= :now
""")
    List<ProductRetryJob> findScheduledJobs(OffsetDateTime now);
}
