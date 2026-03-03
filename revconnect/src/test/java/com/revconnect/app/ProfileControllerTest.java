package com.revconnect.app;

import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "newbiztest")
    public void testNewBusinessProfileError() throws Exception {
        User user = new User();
        user.setUsername("newbiztest");
        user.setEmail("newbiztest@test.com");
        user.setPassword("123");
        user.setRole(Role.BUSINESS);
        userRepository.save(user);

        try {
            mockMvc.perform(get("/users"))
                    .andDo(print());
        } finally {
            userRepository.delete(user);
        }
    }
}
