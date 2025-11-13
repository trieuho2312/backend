package com.bkplatform.service;

import com.bkplatform.dto.RegisterRequest;
import com.bkplatform.model.Role;
import com.bkplatform.model.User;
import com.bkplatform.repository.UserRepository;
import com.bkplatform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public boolean isHustEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@hust.edu.vn");
    }

    @Transactional
    public User register(RegisterRequest req) {
        if (!isHustEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email phải thuộc miền @hust.edu.vn");
        }
        User user = User.builder()
                .username(req.getUsername())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public String generateToken(User user) {
        var claims = new HashMap<String, Object>();
        claims.put("role", user.getRole().name());
        return jwtUtil.generateToken(user.getUsername(), claims);
    }
}
