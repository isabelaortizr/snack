package com.upb.snack.controller;

import com.upb.snack.dto.LoginRequest;
import com.upb.snack.dto.LoginResponse;
import com.upb.snack.entity.User;
import com.upb.snack.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticate(request.getId(), request.getPassword());
            LoginResponse response = new LoginResponse(
                    user.getId(),
                    user.getNombre(),
                    user.getRol()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Contrase?a incorrecta
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            // Usuario no encontrado u otro error
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

