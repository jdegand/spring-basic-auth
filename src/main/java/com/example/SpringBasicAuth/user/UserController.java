package com.example.SpringBasicAuth.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SpringBasicAuth.ChangePasswordRequest.ChangePasswordRequest;
import com.example.SpringBasicAuth.user.converter.UserDtoToUserConverter;
import com.example.SpringBasicAuth.user.converter.UserToUserDtoConverter;
import com.example.SpringBasicAuth.user.dto.UserDto;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

    private final UserService userService;

    private final UserDtoToUserConverter userDtoToUserConverter; // Convert userDto to user.

    private final UserToUserDtoConverter userToUserDtoConverter; // Convert user to userDto.

    public UserController(UserService userService, UserDtoToUserConverter userDtoToUserConverter,
            UserToUserDtoConverter userToUserDtoConverter) {
        this.userService = userService;
        this.userDtoToUserConverter = userDtoToUserConverter;
        this.userToUserDtoConverter = userToUserDtoConverter;
    }

    @GetMapping
    public List<UserDto> findAllUsers() {
        List<User> foundUsers = this.userService.findAll();

        // Convert foundUsers to a list of UserDtos.
        List<UserDto> userDtos = foundUsers.stream()
                .map(this.userToUserDtoConverter::convert)
                .collect(Collectors.toList());

        // Note that UserDto does not contain password field.
        return userDtos;
    }

    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable Integer id) {
        User foundUser = this.userService.findById(id);
        UserDto userDto = this.userToUserDtoConverter.convert(foundUser);
        return userDto;
    }

    /**
     * We are not using UserDto, but User, since we require password.
     *
     * @param newUser
     * @return
     */
    @PostMapping
    public UserDto addUser(@RequestBody User newUser) {
        User savedUser = this.userService.save(newUser);
        UserDto savedUserDto = this.userToUserDtoConverter.convert(savedUser);
        return savedUserDto;
    }

    // We are not using this to update password, need another changePassword method
    // in this class.
    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        User update = this.userDtoToUserConverter.convert(userDto);
        User updatedHogwartsUser = this.userService.update(id, update);
        UserDto updatedUserDto = this.userToUserDtoConverter.convert(updatedHogwartsUser);
        return updatedUserDto;
    }

    @PostMapping("/reset")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        UserPrincipal user = (UserPrincipal) this.userService.loadUserByUsername(changePasswordRequest.getUsername());

        if (!this.userService.oldPasswordIsValid(user, changePasswordRequest.getOldPassword())) {
            return new ResponseEntity<String>("Incorrect old Password", HttpStatus.BAD_REQUEST);
        }

        // could save into a variable (User userInfo =) or convert to a UserDTO to send
        // back to client
        this.userService.updatePassword(user, changePasswordRequest.getNewPassword());

        return new ResponseEntity<String>("Password changed successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        this.userService.delete(id);
    }

}