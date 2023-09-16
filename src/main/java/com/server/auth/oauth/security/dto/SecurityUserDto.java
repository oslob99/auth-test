package com.server.auth.oauth.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class SecurityUserDto {

    private Long userId;
    private String name;
    private String email;
    private String role;
}
