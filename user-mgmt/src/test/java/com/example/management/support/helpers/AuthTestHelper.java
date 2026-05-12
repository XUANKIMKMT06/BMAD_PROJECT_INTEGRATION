package com.example.management.support.helpers;

import com.example.management.auth.AuthRequest;
import com.example.management.auth.AuthResponse;
import com.example.management.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthTestHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public AuthTestHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public String registerAndAuthenticate(RegisterRequest registerRequest) throws Exception {
        register(registerRequest);
        return authenticate(new AuthRequest(registerRequest.username(), registerRequest.password()));
    }

    public void register(RegisterRequest registerRequest) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
    }

    public String authenticate(AuthRequest authRequest) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );
        return response.token();
    }
}
