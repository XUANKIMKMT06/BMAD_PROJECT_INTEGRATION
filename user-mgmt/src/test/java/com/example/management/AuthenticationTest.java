package com.example.management;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testFormAuthentication() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "alice@mail.co")
                        .param("password", "pass"))
                .andExpect(status().isOk()) // or redirect status if using default behavior
                .andExpect(authenticated().withUsername("alice@mail.co"));
    }

    @Test
    @Order(1)
    public void registerNewUser() throws Exception {
        String jsonBody = """ 
                {
                    "name": "test",
                    "username": "test@maven.git",
                    "password": "testtest"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertTrue(response.contains("token"));
                });
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertTrue(response.contains("Username already exists"));
                });
    }
    @Test
    public void testJsonAuthentication() throws Exception {
        String jsonBody = """ 
                {
                    "username": "test@maven.git",
                    "password": "testtest"
                }
                """;

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("test@maven.git"))
                .andExpect(result -> {
                    String token = result.getResponse().getContentAsString();
                    assertTrue(token.contains("token"));
                });
    }

    @Test
    public void LoginApiV1_wrongPassword() throws Exception {
        String jsonBody = """ 
                {
                    "username": "John.Doe@mail.co",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized());
    }
    @Test
    public void LoginApiV1_emptyPassword() throws Exception {
        String jsonBody = """ 
                {
                    "username": "John.Doe@mail.co",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void registerShortPassword() throws Exception {
        String jsonBody = """ 
                {
                    "name": "new user",
                    "username": "John.Doe@mail.co",
                    "password": "pass"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void registerEmptyFields() throws Exception {
        String jsonBody = """ 
                {
                    "name": "",
                    "username": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertTrue(response.contains("Empty"));
                });
    }
    @Test
    public void captialEmail() throws Exception {
        String jsonBody = """ 
                {
                    "name": "Hello",
                    "username": "TEST@GIT.GIT",
                    "password": "TESTtest"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        jsonBody = """ 
                {
                    "username": "TEST@GIT.GIT",
                    "password": "TESTtest"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                //.andExpect(authenticated().withUsername("test@git.git")); turns out spring does not update security context in stateless
                .andExpect(result -> {
                    var response = result.getResponse().getContentAsString();
                    assertTrue(response.contains("test@git.git") && response.contains("token"));
                });
    }
    @Test
    public void credentialsWithSpaces() throws Exception {
        String jsonBody = """ 
                {
                    "name": "Hello",
                    "username": "credintialsWithSpaces@git.git",
                    "password": "Test test"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deletingUser() throws Exception {
        AtomicReference<String> token = new AtomicReference<>();
        String jsonBody = """ 
                {
                    "username": "alice@mail.co",
                    "password": "pass"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                        .andExpect(result -> {
                            String response = result.getResponse().getContentAsString();
                            assertTrue(response.contains("token"));
                            token.set(response.split("token")[1].split("\"")[2]);
                            System.out.println(token);
                        });
        mockMvc.perform(delete("/delete/bob@mail.co").header("Authorization", "Bearer " + token))
                .andExpect(result ->
                        assertEquals(
                                "Deleted",
                                result
                                        .getResponse()
                                        .getContentAsString())
                );
        mockMvc.perform(delete("/delete/bob@mail.co").header("Authorization", "Bearer " + token))
                .andExpect(result ->
                        assertEquals(
                                "Not Found",
                                result
                                        .getResponse()
                                        .getContentAsString())
                );
    }
}
