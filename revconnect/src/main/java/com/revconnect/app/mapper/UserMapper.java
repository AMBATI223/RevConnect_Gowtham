package com.revconnect.app.mapper;

import com.revconnect.app.dto.RegistrationDTO;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static User fromRegistrationDTO(RegistrationDTO dto, String encodedPassword) {
        Role role = Role.USER;
        if (dto.getRole() != null) {
            try {
                role = Role.valueOf(dto.getRole().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                role = Role.USER;
            }
        }
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(encodedPassword != null ? encodedPassword : dto.getPassword())
                .role(role)
                .build();
    }

    public static RegistrationDTO toRegistrationDTO(User user) {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        return dto;
    }
}

