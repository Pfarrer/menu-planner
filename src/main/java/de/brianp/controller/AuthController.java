package de.brianp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/login/success")
    public String loginSuccess() {
        return "forward:/login-success.html";
    }

    @GetMapping("/login/failure")
    public String loginFailure() {
        return "forward:/login-failure.html";
    }
}
