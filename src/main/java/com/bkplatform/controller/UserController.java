package com.bkplatform.controller;

import com.bkplatform.dto.UserProfileResponse;
import com.bkplatform.model.User;
import com.bkplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    /**
     * ✅ FIX: Dùng DTO thay vì anonymous class
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        User u = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        UserProfileResponse response = new UserProfileResponse(
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().name()
        );

        return ResponseEntity.ok(response);
    }
}