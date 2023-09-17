package com.server.auth.config;

import com.server.auth.oauth.handler.OAuth2FailureHandler;
import com.server.auth.oauth.handler.OAuth2SuccessHandler;
import com.server.auth.oauth.service.OAuth2UserService;
import com.server.auth.common.filter.JwtAuthFilter;
import com.server.auth.common.exception.JwtFilterException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuth2SuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2UserService OAuth2UserService;
    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2FailureHandler oAuth2LoginFailureHandler;
    private final JwtFilterException jwtExceptionFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable() // HTTP 기본 인증을 비활성화
                .cors().and() // CORS 활성화
                .csrf().disable() // CSRF 보호 기능 비활성화
                .formLogin().disable() // form 로그인 비활성화
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // URL 인증 여부 Part
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/token/**").permitAll()
                .anyRequest().authenticated()
                .and()

                // OAuth2 Part
                .oauth2Login()
                .userInfoEndpoint().userService(OAuth2UserService) // OAuth2 로그인시 사용자 정보를 가져오는 엔드포인트와 사용자 서비스를 설정
                .and()
                .failureHandler(oAuth2LoginFailureHandler) // OAuth2 로그인 실패시 처리할 핸들러를 지정해준다.
                .successHandler(oAuth2LoginSuccessHandler); // OAuth2 로그인 성공시 처리할 핸들러를 지정해준다.


        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가한다.
        return http
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthFilter.class)
                .build();
    }

}