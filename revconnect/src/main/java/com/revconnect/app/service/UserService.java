package com.revconnect.app.service;

import com.revconnect.app.dto.RegistrationDTO;
import com.revconnect.app.entity.User;

public interface UserService {
    void registerUser(RegistrationDTO dto);

    User findByUsername(String username);
}
