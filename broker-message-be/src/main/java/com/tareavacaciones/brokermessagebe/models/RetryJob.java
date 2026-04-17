package com.tareavacaciones.brokermessagebe.models;

public interface RetryJob {
    String getStepStatus();
    void setStepStatus(String stepStatus);
    // Add other common methods if needed, but for now this is enough for the new requirement
}
