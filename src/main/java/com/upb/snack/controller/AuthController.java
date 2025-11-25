package com.upb.snack.controller;

import com.upb.snack.dto.LoginRequest;
import com.upb.snack.dto.LoginResponse;
import com.upb.snack.entity.User;
import com.upb.snack.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getId(), request.getPassword());
        LoginResponse response = new LoginResponse(user.getId(), user.getNombre(), "Login exitoso");
        return ResponseEntity.ok(response);
    }
}

