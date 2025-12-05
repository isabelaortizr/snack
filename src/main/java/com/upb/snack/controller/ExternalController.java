package com.upb.snack.controller;

import com.upb.snack.dto.GenerateCodeResponse;
import com.upb.snack.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external")
public class ExternalController {

    private final VerificationCodeService verificationCodeService;

    @Value("${external.api-key}")
    private String externalApiKey;

    public ExternalController(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/codes")
    public ResponseEntity<GenerateCodeResponse> generateCode(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam(required = false) String clientId
    ) {
        // Validación simple por API key
        if (!externalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        GenerateCodeResponse response = verificationCodeService.generateCode(clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
