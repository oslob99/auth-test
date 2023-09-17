package com.server.auth.user;

import com.server.auth.oauth.security.dto.SecurityUserDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @GetMapping("/code")
    private ResponseEntity<?> welcome(
            String accessToken
    ){

        log.info("AccessToken : {}", accessToken);

       return ResponseEntity.ok(accessToken);
    }

    @GetMapping("/auth")
    private ResponseEntity<?> auth(
            @AuthenticationPrincipal SecurityUserDto dto
            ){

        log.info("dto : {]", dto);

        return ResponseEntity.ok(dto);
    }



}
