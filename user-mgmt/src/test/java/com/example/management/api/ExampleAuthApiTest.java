package com.example.management.api;

import com.example.management.auth.RegisterRequest;
import com.example.management.support.factories.UserRequestFactory;
import com.example.management.support.fixtures.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExampleAuthApiTest extends BaseIntegrationTest {

    @Test
    void givenNewUser_whenRegisterAndAuthenticate_thenReturnsJwtToken() throws Exception {
        RegisterRequest registerRequest = UserRequestFactory.registerRequest();

        ResultActions registerResult = api().postJson("/api/v1/auth/register", registerRequest);
        registerResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value(registerRequest.username()));

        api().postJson("/api/v1/auth/authenticate", UserRequestFactory.authRequest(registerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value(registerRequest.username()));
    }
}
