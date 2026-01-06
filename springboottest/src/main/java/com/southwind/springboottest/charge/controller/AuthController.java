package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.User;
import com.southwind.springboottest.charge.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Data
    public static class LoginReq {
        private String username;
        /** ADMIN / STUDENT / TEACHER */
        private String role;
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginReq req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("username不能为空");
        }
        String role = (req.getRole() == null || req.getRole().trim().isEmpty()) ? "STUDENT" : req.getRole();
        return userRepository.findByUsername(req.getUsername().trim())
                .map(u -> {
                    u.setRole(role);
                    return userRepository.save(u);
                })
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(req.getUsername().trim());
                    u.setRole(role);
                    return userRepository.save(u);
                });
    }
}
