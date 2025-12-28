package com.jobPortal.jobPortal.repository;

import com.jobPortal.jobPortal.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, Integer> {

    Page<Job> findByIsActiveTrue(Pageable pageable);

    Page<Job> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title, Pageable pageable);

    Page<Job> findByLocationContainingIgnoreCaseAndIsActiveTrue(String location, Pageable pageable);

    Page<Job> findByCompanyContainingIgnoreCaseAndIsActiveTrue(String company, Pageable pageable);
}
