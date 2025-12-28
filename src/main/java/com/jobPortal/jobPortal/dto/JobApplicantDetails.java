package com.jobPortal.jobPortal.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Data
public class JobApplicantDetails {

    private int jobId;
    private String applicantEmail;
    private String applicantName;
    private Instant appliedAt;
    private String recruiterEmail;
}
