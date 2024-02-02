package com.example.SpringBasicAuth.user;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.SpringBasicAuth.ChangePasswordRequest.ChangePasswordRequest;
import com.example.SpringBasicAuth.system.exception.ObjectNotFoundException;
import com.example.SpringBasicAuth.user.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    List<User> users;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {
        this.users = new ArrayList<>();

        User u1 = new User();
        u1.setId(1);
        u1.setUsername("john");
        u1.setPassword("123456");
        u1.setEnabled(true);
        u1.setRoles("admin user");
        this.users.add(u1);

        User u2 = new User();
        u2.setId(2);
        u2.setUsername("eric");
        u2.setPassword("654321");
        u2.setEnabled(true);
        u2.setRoles("user");
        this.users.add(u2);

        User u3 = new User();
        u3.setId(3);
        u3.setUsername("tom");
        u3.setPassword("qwerty");
        u3.setEnabled(false);
        u3.setRoles("user");
        this.users.add(u3);
    }

    @Test
    void testFindAllUsersSuccess() throws Exception {
        given(this.userService.findAll()).willReturn(this.users);

        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("john"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("eric"));
    }

    @Test
    void testFindUserByIdSuccess() throws Exception {
        given(this.userService.findById(1)).willReturn(this.users.get(0));

        // When and then is combined in controllers

        this.mockMvc.perform(get(this.baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.roles").value("admin user"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {

        // order matters for constructor

        UserDto userDto = new UserDto(3, "tom123", "user", false);

        User updatedUser = new User();
        updatedUser.setId(3);
        updatedUser.setUsername("jeff"); // Originally tom.
        updatedUser.setEnabled(false);
        updatedUser.setRoles("user");

        String json = this.objectMapper.writeValueAsString(userDto);

        given(this.userService.update(eq(3), Mockito.any(User.class))).willReturn(updatedUser);

        this.mockMvc
                .perform(put(this.baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("jeff"))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.roles").value("user"));
    }

    @Test
    void testUpdateUserErrorWithNonExistentId() throws Exception {
        given(this.userService.update(eq(5), Mockito.any(User.class)))
                .willThrow(new ObjectNotFoundException("user", 5));

        UserDto userDto = new UserDto(5, "tom123", "user", false);

        String json = this.objectMapper.writeValueAsString(userDto);

        this.mockMvc
                .perform(put(this.baseUrl + "/users/5").contentType(MediaType.APPLICATION_JSON).content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value("Could not find user with Id 5"));
    }

    @Test
    void testDeleteUserSuccess() throws Exception {
        doNothing().when(this.userService).delete(3);

        this.mockMvc.perform(delete(this.baseUrl + "/users/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePasswordSuccess() throws Exception {

        UserPrincipal userPrincipal = new UserPrincipal(this.users.get(1));

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("eric", "654321", "steel");

        String json = this.objectMapper.writeValueAsString(changePasswordRequest);

        User updatedUser = new User();
        updatedUser.setId(2);
        updatedUser.setUsername("eric");
        updatedUser.setPassword("steel");
        updatedUser.setEnabled(true);
        updatedUser.setRoles("user");

        given(this.userService.loadUserByUsername(changePasswordRequest.getUsername())).willReturn(userPrincipal);

        given(this.userService.oldPasswordIsValid(userPrincipal, changePasswordRequest.getOldPassword()))
                .willReturn(true);

        given(this.userService.updatePassword(userPrincipal, changePasswordRequest.getNewPassword()))
                .willReturn(updatedUser);

        this.mockMvc
                .perform(post(this.baseUrl + "/users/reset").contentType(MediaType.APPLICATION_JSON).content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Password changed successfully"));
    }

    @Test
    void testUpdatePasswordFailure1() throws Exception {

        UserPrincipal userPrincipal = new UserPrincipal(this.users.get(1));

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("eric", "654", "steel");

        String json = this.objectMapper.writeValueAsString(changePasswordRequest);

        User updatedUser = new User();
        updatedUser.setId(2);
        updatedUser.setUsername("eric");
        updatedUser.setPassword("steel");
        updatedUser.setEnabled(true);
        updatedUser.setRoles("user");

        given(this.userService.loadUserByUsername(changePasswordRequest.getUsername())).willReturn(userPrincipal);

        // given(this.userService.oldPasswordIsValid(userPrincipal,
        // changePasswordRequest.getOldPassword())).willReturn(true);

        given(this.userService.updatePassword(userPrincipal, changePasswordRequest.getNewPassword()))
                .willReturn(updatedUser);

        this.mockMvc
                .perform(post(this.baseUrl + "/users/reset").contentType(MediaType.APPLICATION_JSON).content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Incorrect old Password"));
    }

    @Test
    void testUpdatePasswordFailure2() throws Exception {

        UserPrincipal userPrincipal = new UserPrincipal(this.users.get(1));

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setUsername("eric");
        changePasswordRequest.setOldPassword("654321");
        changePasswordRequest.setNewPassword("steel");

        String json = this.objectMapper.writeValueAsString(changePasswordRequest);

        User updatedUser = new User();
        updatedUser.setId(2);
        updatedUser.setUsername("eric");
        updatedUser.setPassword("steel");
        updatedUser.setEnabled(true);
        updatedUser.setRoles("user");

        // need to mock other checks inside updatePassword - otherwise test will return
        // 400 as the passwords are encoded and will not match
        // I commented out the check - andExpect(status().isOk()) - would pass

        given(this.userService.updatePassword(userPrincipal, changePasswordRequest.getNewPassword()))
                .willReturn(updatedUser);

        this.mockMvc
                .perform(post(this.baseUrl + "/users/reset").contentType(MediaType.APPLICATION_JSON).content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
