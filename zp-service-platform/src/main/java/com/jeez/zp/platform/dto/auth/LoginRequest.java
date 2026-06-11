package com.jeez.zp.platform.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String mobile;
    private String email;
    private String password;
}
