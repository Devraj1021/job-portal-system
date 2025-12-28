package com.jobPortal.jobPortal.service;

import com.jobPortal.jobPortal.entity.User;
import com.jobPortal.jobPortal.exception.UnauthenticatedException;
import com.jobPortal.jobPortal.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;

@Service
@RequestMapping("/users")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public String saveNewUser(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));
            saveUser(user);
            return "User created";
        } catch (Exception e) {
            return "Could not create user due to " + e;
        }
    }

    public String saveRecruiter(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER", "RECRUITER"));
            saveUser(user);
            return "Recruiter created";
        } catch (Exception e) {
            return "Could not create user due to " + e;
        }
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Page<User> getAll(Pageable pageable) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User userInDb = userRepository.findByUserName(userName);
        if (userInDb == null) {
            throw new UnauthenticatedException("User not found");
        }

        if (userInDb.getRoles() == null || !userInDb.getRoles().contains("ADMIN")) {
            throw new AccessDeniedException("Only admin can view all users");
        }

        return userRepository.findAll(pageable);
    }

    public User findById() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User userInDb = userRepository.findByUserName(userName);
        if (userInDb == null) {
            throw new UnauthenticatedException("User not found");
        }
        return userInDb;
    }

    public User updateUser(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User userInDb = userRepository.findByUserName(userName);
        if (userInDb == null) {
            throw new UnauthenticatedException("User not found");
        }
        userInDb.setUserName(user.getUserName());
        userInDb.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(userInDb);
    }

    public void deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthenticatedException("You must be logged in");
        }

        String userName = authentication.getName();
        User userInDb = userRepository.findByUserName(userName);
        if (userInDb == null) {
            throw new UnauthenticatedException("User not found");
        }

        userRepository.deleteByUserName(userInDb.getName());
    }

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

}
