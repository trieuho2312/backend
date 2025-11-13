package com.bkplatform.controller;

import com.bkplatform.model.User;
import com.bkplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        User u = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(new Object() {
            public Long userId = u.getUserId();
            public String username = u.getUsername();
            public String fullName = u.getFullName();
            public String email = u.getEmail();
            public String role = u.getRole().name();
        });
    }
}
