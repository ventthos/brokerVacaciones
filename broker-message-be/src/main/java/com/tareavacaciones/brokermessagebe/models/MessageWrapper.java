package com.tareavacaciones.brokermessagebe.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageWrapper {
    private Map<String, Object> data;
    private StepStatus sendEmail;
    private StepStatus updateRetryJobs;
}
