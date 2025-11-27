package com.upb.snack.dto;

public class LoginRequest {

    private Long id;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(Long id, String password) {
        this.id = id;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


