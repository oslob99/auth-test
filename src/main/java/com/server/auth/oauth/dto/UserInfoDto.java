package com.server.auth.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserInfoDto {

    private Long userId;
    private String name;
    private String email;
    private String role;
}
