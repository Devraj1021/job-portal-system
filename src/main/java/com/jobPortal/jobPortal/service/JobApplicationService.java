package com.jobPortal.jobPortal.service;

import com.jobPortal.jobPortal.dto.JobApplicantDetails;
import com.jobPortal.jobPortal.dto.JobApplicationDto;
import com.jobPortal.jobPortal.entity.ApplicationStatus;
import com.jobPortal.jobPortal.entity.Job;
import com.jobPortal.jobPortal.entity.JobApplication;
import com.jobPortal.jobPortal.entity.User;
import com.jobPortal.jobPortal.exception.DuplicateResourceException;
import com.jobPortal.jobPortal.exception.ResourceNotFoundException;
import com.jobPortal.jobPortal.exception.UnauthenticatedException;
import com.jobPortal.jobPortal.repository.JobApplicationRepository;
import com.jobPortal.jobPortal.repository.JobRepository;
import com.jobPortal.jobPortal.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private JobApplicantDetails jobApplicantDetails;

    @Autowired
    private KafkaTemplate<String, JobApplicantDetails> kafkaTemplate;

    @Transactional
    public JobApplication applyToJob(int jobId, JobApplication jobApplication) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->  new ResourceNotFoundException("Job not found"));
        if (!job.isActive()) {
            throw new IllegalStateException("Cannot apply to an inactive job");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UnauthenticatedException("User not found");
        }

        // Prevent duplicate application (race: DB unique constraint as backup)
        if (applicationRepository.existsByJobIdAndApplicantId(jobId, user.getId())) {
            throw new DuplicateResourceException("Application already exists for this user and job");
        }

        JobApplication app = new JobApplication();
        app.setJob(job);
        app.setApplicant(user);
        app.setCoverLetter(jobApplication.getCoverLetter());
        app.setResumeUrl(jobApplication.getResumeUrl());
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(Instant.now());

        JobApplication saved = applicationRepository.save(app);

        jobApplicantDetails.setJobId(jobId);
        jobApplicantDetails.setApplicantEmail(userName);
        jobApplicantDetails.setApplicantName(user.getName());
        jobApplicantDetails.setAppliedAt(app.getAppliedAt());
        jobApplicantDetails.setRecruiterEmail(job.getCreatedBy().getUserName());

        // Optional: notify recruiter/email, publish event, etc.
        kafkaTemplate.send("job-applications", job.getCreatedBy().getUserName(), jobApplicantDetails);
        return saved;
    }

    @Transactional
    public List<JobApplicationDto> getApplicationsById(int jobId) throws AccessDeniedException {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->  new ResourceNotFoundException("Job not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UnauthenticatedException("User not found");
        }

        if (user.getRoles() == null || !user.getRoles().contains("RECRUITER")) {
            throw new AccessDeniedException("Only recruiters can see applications for a job");
        }

        List<JobApplication> applications = applicationRepository.findByJobId(jobId);

        return applications.stream().map(app -> {
            JobApplicationDto dto = new JobApplicationDto();
            dto.setId(app.getId());
            dto.setJobId(app.getJob().getId());
            dto.setApplicantId(app.getApplicant().getId());
            dto.setApplicantEmail(app.getApplicant().getUserName());
            dto.setApplicantName(app.getApplicant().getName());
            dto.setCoverLetter(app.getCoverLetter());
            dto.setResumeUrl(app.getResumeUrl());
            dto.setStatus(app.getStatus() != null ? app.getStatus().name() : null);
            dto.setAppliedAt(app.getAppliedAt());
            return dto;
        }).collect(Collectors.toList());
    }

}
