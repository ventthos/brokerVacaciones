package com.tareavacaciones.brokermessagebe.models;

import java.time.OffsetDateTime;

public interface RetryJob {
    String getStepStatus();
    void setStepStatus(String stepStatus);
    int getRetryCount();
    void setRetryCount(int retryCount);
    String getStatus();
    void setStatus(String status);
    String getErrorMessage();
    void setErrorMessage(String errorMessage);
    void setNextRunAt(OffsetDateTime nextRunAt);
}
