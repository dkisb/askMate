package com.codecool.askmateoop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=test-secret"
})

@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AskMateOOPApplicationIT {

    @Test
    void contextLoads() {
    }
}
