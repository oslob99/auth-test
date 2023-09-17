package com.server.auth.oauth.service;

import com.server.auth.user.domain.Role;
import com.server.auth.user.domain.User;
import com.server.auth.user.dto.request.UserSignUpDto;
import com.server.auth.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService implements org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();

        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2Attribute oAuth2Attribute =
                OAuth2Attribute.of(userNameAttributeName, oAuth2User.getAttributes());

//        log.info("OAUTH : {} ", oAuth2Attribute);
//        log.info("name : {} , profile : {} ", oAuth2Attribute.getAttributes().get("name"), oAuth2Attribute.getAttributes().get("picture"));

        Map<String, Object> userAttribute = oAuth2Attribute.convertToMap();

        // 로그인한 email
        String email = (String) userAttribute.get("email");

        // 가입 여부
        Optional<User> findMember = userService.findByEmail(email);

        if (findMember.isEmpty()) {

            // 가입되지 않은 유저라면 바로 회원 등록
            userService.save(UserSignUpDto.builder()
                    .email(email)
                    .name(oAuth2Attribute.getAttributes().get("name").toString())
                    .profile(oAuth2Attribute.getAttributes().get("picture").toString())
                    .build());


            // 기본 권한 USER로 지정
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(Role.USER.toString())),
                    userAttribute, "email");
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(String.valueOf(findMember.get().getRole()))),
                userAttribute, "email");

    }
}