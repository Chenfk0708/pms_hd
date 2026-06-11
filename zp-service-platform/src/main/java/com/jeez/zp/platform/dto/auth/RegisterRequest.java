package com.jeez.zp.platform.dto.auth;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;
    private String password;
    private String nickName;
    private String mobile;
    private String email;
    private Long campId;
    private Long roleId;
}
