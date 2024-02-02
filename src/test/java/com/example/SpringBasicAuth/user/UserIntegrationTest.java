package com.example.SpringBasicAuth.user;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.example.SpringBasicAuth.ChangePasswordRequest.ChangePasswordRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.json.JSONObject;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @Value("${api.endpoint.base-url}")
        String baseUrl;

        String token;

        @BeforeEach
        void setUp() throws Exception {
                // ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl +
                // "/auth/login").header(HttpHeaders.AUTHORIZATION,
                // "Basic " + Base64Utils.encodeToString("john:123456".getBytes())));
                ResultActions resultActions = this.mockMvc
                                .perform(post(this.baseUrl + "/users/login").with(httpBasic("john", "123456")));

                MvcResult mvcResult = resultActions.andDo(print()).andReturn();
                String contentAsString = mvcResult.getResponse().getContentAsString();
                JSONObject json = new JSONObject(contentAsString);
                this.token = "Bearer " + json.getString("token");
        }

        // Can use @Order - to order tests or use @DirtiesContext to reset the database
        // before each test
        // @DirtiesContext - slower

        // Not using a standard result object - with well-defined keys - can make
        // testing more difficult

        @Test
        @DisplayName("findAllUsers (GET)")
        // Reset H2 database before calling this test case. Size will be different after
        // an add / delete test.
        @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
        void testFindAllUsersSuccess() throws Exception {
                this.mockMvc
                                .perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token)) // have to watch
                                                                                                // parentheses and add
                                                                                                // inside perform
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$", Matchers.hasSize(3)));
        }

        @Test
        @DisplayName("findUserById (GET)")
        @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
        void testFindUserByIdSuccess() throws Exception {
                this.mockMvc
                                .perform(get(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token))
                                .andExpect(jsonPath("$.id").value(2))
                                .andExpect(jsonPath("$.username").value("eric"));
        }

        @Test
        @DisplayName("findUserById (GET) Failure")
        void testFindUserByIdFailure() throws Exception {
                this.mockMvc
                                .perform(get(this.baseUrl + "/users/8").accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token))
                                .andExpect(jsonPath("$").value("Could not find user with Id 8"));
        }

        @Test
        @DisplayName("addUser (POST)")
        void testAddUser() throws Exception {

                User payload = new User();

                payload.setUsername("Bobby");
                payload.setRoles("user");
                payload.setPassword("greenbay");
                payload.setEnabled(true);

                String json = this.objectMapper.writeValueAsString(payload);

                this.mockMvc
                                .perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON)
                                                .content(json).accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token))
                                // .andExpect(jsonPath("$").value(""));
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("Bobby"))
                                .andExpect(jsonPath("$.roles").value("user"))
                                .andExpect(jsonPath("$.enabled").value("true")); // can't really check password
        }

        // Need to add validation to test addUser POST failure

        @Test
        @DisplayName("updateUser (PUT)")
        void testUpdateUser() throws Exception {

                /*
                 * u3.setUsername("tom");
                 * u3.setPassword("qwerty");
                 * u3.setEnabled(false);
                 * u3.setRoles("user");
                 */

                User payload = new User();

                payload.setId(3);
                payload.setUsername("tom");
                payload.setPassword("qwerty");
                payload.setEnabled(true);
                payload.setRoles("user");

                String json = this.objectMapper.writeValueAsString(payload);

                this.mockMvc
                                .perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON)
                                                .content(json).accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("tom"))
                                .andExpect(jsonPath("$.roles").value("user"))
                                .andExpect(jsonPath("$.enabled").value("true"));
        }

        // need validation to test update failure - if id doesn't exist, you still get a
        // 200

        @Test
        @DisplayName("deleteUser (DELETE)")
        @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
        void testDeleteUser() throws Exception {

                this.mockMvc
                                .perform(delete(this.baseUrl + "/users/3").accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deleteUser (DELETE) Failure")
        void testDeleteUserFailure() throws Exception {

                this.mockMvc
                                .perform(delete(this.baseUrl + "/users/99").accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("changePassword (POST)")
        @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
        void testUpdatePassword() throws Exception {

                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
                changePasswordRequest.setUsername("john");
                changePasswordRequest.setOldPassword("123456");
                changePasswordRequest.setNewPassword("123456789");

                String json = this.objectMapper.writeValueAsString(changePasswordRequest);

                this.mockMvc
                                .perform(post(this.baseUrl + "/users/reset").contentType(MediaType.APPLICATION_JSON)
                                                .content(json).accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("Password changed successfully"));
        }

        @Test
        @DisplayName("changePassword (POST) Failure")
        @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
        void testUpdatePasswordFailure() throws Exception {

                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
                changePasswordRequest.setUsername("john");
                changePasswordRequest.setOldPassword("12345"); // not correct
                changePasswordRequest.setNewPassword("123456789");

                String json = this.objectMapper.writeValueAsString(changePasswordRequest);

                this.mockMvc
                                .perform(post(this.baseUrl + "/users/reset").contentType(MediaType.APPLICATION_JSON)
                                                .content(json).accept(MediaType.APPLICATION_JSON)
                                                .header(HttpHeaders.AUTHORIZATION, this.token))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$").value("Incorrect old Password"));
        }

}
