package com.jeez.zp.platform.dto.auth;

import lombok.Data;

@Data
public class AccountUpdateRequest {

    private String nickName;
    private String email;
    private String wechat;
    private String avatarUrl;
    private String oldPassword;
    private String newPassword;
}
