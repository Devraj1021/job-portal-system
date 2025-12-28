package com.jobPortal.jobPortal.repository;

import com.jobPortal.jobPortal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUserName(String username);

    Page<User> findAll(Pageable pageable);
    //Optional<User> findById(UUID id);

    void deleteByUserName(String username);
}
