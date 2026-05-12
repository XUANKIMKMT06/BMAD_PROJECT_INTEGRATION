package com.example.management.support.fixtures;

import com.example.management.support.helpers.ApiTestClient;
import com.example.management.support.helpers.AuthTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected ApiTestClient api() {
        return new ApiTestClient(mockMvc, objectMapper);
    }

    protected AuthTestHelper auth() {
        return new AuthTestHelper(mockMvc, objectMapper);
    }
}
