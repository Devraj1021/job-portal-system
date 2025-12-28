package com.jobPortal.jobPortal.service;

import com.jobPortal.jobPortal.entity.Job;
import com.jobPortal.jobPortal.entity.User;
import com.jobPortal.jobPortal.exception.ResourceNotFoundException;
import com.jobPortal.jobPortal.exception.UnauthenticatedException;
import com.jobPortal.jobPortal.repository.JobApplicationRepository;
import com.jobPortal.jobPortal.repository.JobRepository;
import com.jobPortal.jobPortal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    // can be accessed by both recruiter and job_seeker
    // get job by id
    public Job findById(int jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        return job;
    }

    //@PreAuthorize("hasRole('RECRUITER')")
    public Job createJob(Job job) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UnauthenticatedException("Recruiter not found");
        }

        if (user.getRoles() == null || !user.getRoles().contains("RECRUITER")) {
            throw new AccessDeniedException("Only recruiters can create jobs");
        }

        job.setPostedAt(LocalDate.now());
        job.setActive(true);
        job.setCreatedBy(user);
        return jobRepository.save(job);
    }

    public Job updateJob(int jobId, Job updated) throws AccessDeniedException {
        Job existing = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // check owner
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UnauthenticatedException("User not found");
        }

        User owner = existing.getCreatedBy();
        if (owner == null || !owner.getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the recruiter who created this job can update it");
        }

        // apply updates
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setLocation(updated.getLocation());
        existing.setSalary(updated.getSalary());
        existing.setCompany(updated.getCompany());
        existing.setActive(updated.isActive());

        return jobRepository.save(existing);
    }

    public void deleteJob(int jobId) throws AccessDeniedException {
        Job existing = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UnauthenticatedException("User not found");
        }

        User owner = existing.getCreatedBy();
        if (owner == null || !owner.getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the recruiter who created this job can delete it");
        }

        jobRepository.deleteById(jobId);
    }

    public Page<Job> list(String title, String location, String company, Pageable pageable) {
        if ((title == null || title.isBlank()) &&
                (location == null || location.isBlank()) &&
                (company == null || company.isBlank())) {
            return jobRepository.findByIsActiveTrue(pageable);
        }

        if (title != null && !title.isBlank()) {
            return jobRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(title, pageable);
        }
        if (location != null && !location.isBlank()) {
            return jobRepository.findByLocationContainingIgnoreCaseAndIsActiveTrue(location, pageable);
        }
        if (company != null && !company.isBlank()) {
            return jobRepository.findByCompanyContainingIgnoreCaseAndIsActiveTrue(company, pageable);
        }

        return jobRepository.findByIsActiveTrue(pageable);
    }

}
