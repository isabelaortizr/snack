package com.upb.snack.dto;

import java.time.LocalDateTime;

public class GenerateCodeResponse {

    private String code;
    private LocalDateTime expiresAt;

    public GenerateCodeResponse() { }

    public GenerateCodeResponse(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
