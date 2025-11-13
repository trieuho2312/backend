package com.bkplatform.controller;

import com.bkplatform.dto.RegisterRequest;
import com.bkplatform.dto.AuthResponse;
import com.bkplatform.model.User;
import com.bkplatform.repository.UserRepository;
import com.bkplatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req);
        String token = authService.generateToken(user);
        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole().name());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody com.bkplatform.dto.LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserDetails principal = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        String token = authService.generateToken(user);
        AuthResponse resp = new AuthResponse();
        resp.setToken(token); resp.setUsername(user.getUsername()); resp.setRole(user.getRole().name());
        return ResponseEntity.ok(resp);
    }
}
