package com.example.SpringBasicAuth.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Generate a JSON web token if username and password has been authenticated by
     * the BasicAuthenticationFilter.
     * In summary, this filter is responsible for processing any request that has an
     * HTTP request header of Authorization
     * with an authentication scheme of Basic and a Base64-encoded username:password
     * token.
     * <p>
     * BasicAuthenticationFilter will prepare the Authentication object for this
     * login method.
     * Note: before this login method gets called, Spring Security already
     * authenticated the username and password through Basic Auth.
     * Only successful authentication can make it to this method.
     *
     * @return User information and JSON web token
     */

    @PostMapping("/login")
    public ResponseEntity<Object> getLoginInfo(Authentication authentication) {
        return new ResponseEntity<Object>(this.authService.createLoginInfo(authentication), HttpStatus.OK);
    }

}
