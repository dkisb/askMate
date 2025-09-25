package com.codecool.askmateoop;

import com.codecool.askmateoop.model.payload.dto.user.NewUserDTO;
import com.codecool.askmateoop.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = {"jwt.secret=secret"})
class AskMateOOPApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void contextLoads() {
    }

    @Test
    void createNewUserWhenUsernameIsUniqueShouldReturnSuccess() throws Exception {
        String username = "testuser";
        String password = "password";
        String email = "testuser@test.com";
        NewUserDTO newUserDTO = new NewUserDTO(username, password, email);

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void createNewUserWhenUsernameIsNotUniqueShouldReturnFailure() throws Exception {
        String username = "testuser";
        String password = "password";
        String email = "testuser@test.com";
        NewUserDTO newUserDTO = new NewUserDTO(username, password, email);
        userService.createUser(newUserDTO);

        String username2 = "testuser";
        String password2 = "password";
        String email2 = "testuser@test.com";
        NewUserDTO newUserDTO2 = new NewUserDTO(username2, password2, email2);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDTO2)))
                        .andExpect(status().isConflict());
    }}
