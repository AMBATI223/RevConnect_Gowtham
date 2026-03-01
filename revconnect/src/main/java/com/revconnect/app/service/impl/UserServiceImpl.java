package com.revconnect.app.service.impl;

import com.revconnect.app.repository.BusinessProfileRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.dto.RegistrationDTO;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.service.UserService;
import com.revconnect.app.entity.BusinessProfile;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BusinessProfileRepository businessProfileRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerUser(RegistrationDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role role = Role.USER;
        if (dto.getRole() != null) {
            try {
                role = Role.valueOf(dto.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                role = Role.USER;
            }
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .build();

        try {
            userRepository.saveAndFlush(user);

            if (role == Role.BUSINESS || role == Role.CREATOR) {
                BusinessProfile bp = new BusinessProfile(
                        user,
                        dto.getBusinessName() != null ? dto.getBusinessName() : dto.getUsername() + " Business",
                        dto.getIndustry(),
                        dto.getBusinessAddress(),
                        dto.getBusinessHours(),
                        dto.getContactLinks());
                businessProfileRepository.saveAndFlush(bp);
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("An account with this email or username already exists.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create account. Please try again later.");
        }
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
}
