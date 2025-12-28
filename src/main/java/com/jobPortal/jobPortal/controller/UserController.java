package com.jobPortal.jobPortal.controller;

import com.jobPortal.jobPortal.entity.User;
import com.jobPortal.jobPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/admin/all-users")
    public ResponseEntity<Page<User>> findAll(Pageable pageable) throws AccessDeniedException {
        try {
            Page<User> users = userService.getAll(pageable);
            return ResponseEntity.ok(users);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("Only admin can view all users");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<User> findById() {
        User user = userService.findById();
        return ResponseEntity.ok(user);
    }


    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.noContent().build();
    }

}
