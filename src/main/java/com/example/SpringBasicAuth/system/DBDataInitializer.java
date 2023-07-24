package com.example.SpringBasicAuth.system;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.SpringBasicAuth.user.User;
import com.example.SpringBasicAuth.user.UserService;

@Component
public class DBDataInitializer implements CommandLineRunner {

    private final UserService userService;


    public DBDataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        User u1 = new User();
        u1.setId(1);
        u1.setUsername("john");
        u1.setPassword("123456");
        u1.setEnabled(true);
        u1.setRoles("admin user");

        User u2 = new User();
        u2.setId(2);
        u2.setUsername("eric");
        u2.setPassword("654321");
        u2.setEnabled(true);
        u2.setRoles("user");

        User u3 = new User();
        u3.setId(3);
        u3.setUsername("tom");
        u3.setPassword("qwerty");
        u3.setEnabled(false);
        u3.setRoles("user");

        this.userService.save(u1);
        this.userService.save(u2);
        this.userService.save(u3);
    }

}
