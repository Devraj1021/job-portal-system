package com.jobPortal.jobPortal.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Data
public class JobApplicationDto {

    private Long id;
    private int jobId;
    private UUID applicantId;
    private String applicantEmail;
    private String applicantName;
    private String coverLetter;
    private String resumeUrl;
    private String status;
    private Instant appliedAt;
}
