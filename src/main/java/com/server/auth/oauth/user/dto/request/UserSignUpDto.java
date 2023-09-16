package com.server.auth.oauth.user.dto.request;

import com.server.auth.oauth.user.domain.Role;
import com.server.auth.oauth.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
public class UserSignUpDto {

    @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String profile;

    public User toEntity(){
        return User.builder()
                .userEmail(email)
                .userName(name)
                .userProfile(profile)
                .role(Role.USER)
                .build();
    }
}
