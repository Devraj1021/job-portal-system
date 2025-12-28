package com.jobPortal.jobPortal.repository;

import com.jobPortal.jobPortal.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJobIdAndApplicantId(int jobId, UUID applicantId);

    Optional<JobApplication> findByJobIdAndApplicantId(int jobId, UUID applicantId);

    List<JobApplication> findByJobId(int jobId);
}
