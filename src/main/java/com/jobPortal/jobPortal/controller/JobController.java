package com.jobPortal.jobPortal.controller;

import com.jobPortal.jobPortal.dto.JobApplicationDto;
import com.jobPortal.jobPortal.entity.Job;
import com.jobPortal.jobPortal.entity.JobApplication;
import com.jobPortal.jobPortal.service.JobApplicationService;
import com.jobPortal.jobPortal.service.JobService;
import com.jobPortal.jobPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobApplicationService jobApplicationService;

    @Autowired
    private UserService userService;

    // GET /jobs?title=&location=&company=&page=&size=&sort=
    @GetMapping
    public ResponseEntity<Page<Job>> listJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company,
            Pageable pageable) {
        try {
            Page<Job> jobs = jobService.list(title, location, company, pageable);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET /jobs/{id}
    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJobById(@PathVariable int jobId) {
        try {
            Job job = jobService.findById(jobId);
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // POST /jobs/{id}  (RECRUITER)
    @PostMapping("/recruiter")
    public ResponseEntity<Job> createJob(@RequestBody Job job) throws AccessDeniedException {
        try {
            Job created = jobService.createJob(job);
            return ResponseEntity.status(201).body(created);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // PUT /jobs/{jobId}/{userId}  (RECRUITER)
    @PutMapping("/recruiter/{jobId}")
    public ResponseEntity<Job> updateJob(@PathVariable int jobId, @RequestBody Job updatedJob) throws AccessDeniedException {
        try {
            Job updated = jobService.updateJob(jobId, updatedJob);
            return ResponseEntity.status(201).body(updated);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // DELETE /jobs/{jobId}/{userId}  (RECRUITER)
    @DeleteMapping("/recruiter/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable int jobId) throws AccessDeniedException {
        jobService.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/apply/{jobId}")
    public ResponseEntity<JobApplication> applyToJob(@PathVariable int jobId,
                                             @RequestBody JobApplication jobApplication) {
        JobApplication jobApplied = jobApplicationService.applyToJob(jobId, jobApplication);
        return ResponseEntity.ok(jobApplied);
    }

    @GetMapping("/recruiter/{jobId}/applied-candidates")
    public ResponseEntity<List<JobApplicationDto>> getApplicationsById(@PathVariable int jobId) throws AccessDeniedException {
        List<JobApplicationDto> jobApplicationDtos = jobApplicationService.getApplicationsById(jobId);
        return ResponseEntity.ok(jobApplicationDtos);
    }

}
