package com.server.auth.common.util;

import com.server.auth.oauth.dto.UserInfoDto;
import com.server.auth.oauth.refresh.domain.Token;
import com.server.auth.oauth.refresh.service.RefreshTokenService;
import com.server.auth.common.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;
    private final RefreshTokenService tokenService;
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(jwtProperties.getSecret().getBytes());
    }


    public Token generateToken(String email, String role) {
        // refreshToken과 accessToken을 생성한다.
        String refreshToken = generateRefreshToken(email, role);
        String accessToken = generateAccessToken(email, role);

        // 토큰을 Redis에 저장한다.
        tokenService.saveTokenInfo(email, refreshToken, accessToken);
        return new Token(accessToken, refreshToken);
    }

    public String generateRefreshToken(String email, String role) {
        // 토큰의 유효 기간을 밀리초 단위로 설정.
//        long refreshPeriod = 1000L * 60L * 60L * 24L * 14; // 2주
        long refreshPeriod = 1000L * 3L; // 2주
        // 새로운 클레임 객체를 생성하고, 이메일과 역할(권한)을 셋팅
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        // 현재 시간과 날짜를 가져온다.
        Date now = new Date();

        return Jwts.builder()
                // Payload를 구성하는 속성들을 정의한다.
                .setClaims(claims)
                // 발행일자를 넣는다.
                .setIssuedAt(now)
                // 토큰의 만료일시를 설정한다.
                .setExpiration(new Date(now.getTime() + refreshPeriod))
                // 지정된 서명 알고리즘과 비밀 키를 사용하여 토큰을 서명한다.
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }


    public String generateAccessToken(String email, String role) {
//        long tokenPeriod = 1000L * 60L * 30L; // 30분
        long tokenPeriod = 1000L * 60L; // 10초
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        return
                Jwts.builder()
                        // Payload를 구성하는 속성들을 정의한다.
                        .setClaims(claims)
                        // 발행일자를 넣는다.
                        .setIssuedAt(now)
                        // 토큰의 만료일시를 설정한다.
                        .setExpiration(new Date(now.getTime() + tokenPeriod))
                        // 지정된 서명 알고리즘과 비밀 키를 사용하여 토큰을 서명한다.
                        .signWith(SignatureAlgorithm.HS256, secretKey)
                        .compact();

    }

    /**
     * 클라이언트가 보낸 토큰을 디코딩 / 파싱해서 토큰의 위조 여부 확인
     * 토큰을 JSON 으로 파싱해서 클레임을 리턴
     * @param token - 클라이언트가 전송한 인코딩 된 토큰
     * @return - 토큰에서 subject(userIdx, 유저 식별자) 를 꺼내서 반환
     */
    public UserInfoDto validateAndGetTokenUserInfo(String token) {

        log.info("validateAndGetTokenUserInfo / token: {}", token);

        Claims claims = Jwts.parserBuilder()
                // 토큰 발급자의 발급 당시 서명을 넣어줌
                .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
                // 서명 위조 검사 - 클라이언트 토큰의 서명과 서버 발급 당시 서명을 비교
                // 위조 X 경우 : payload(Claims) 를 리턴
                // 위조 O 경우 : 예외 발생
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UserInfoDto.builder()
                .userId(Long.valueOf(claims.getSubject()))
                .email(claims.get("email", String.class))
                .role((String) claims.get("role"))
                .build();
    }



    public boolean verifyToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey) // 비밀키를 설정하여 파싱한다.
                    .parseClaimsJws(token);  // 주어진 토큰을 파싱하여 Claims 객체를 얻는다.
            // 토큰의 만료 시간과 현재 시간비교
            return claims.getBody()
                    .getExpiration()
                    .after(new Date());  // 만료 시간이 현재 시간 이후인지 확인하여 유효성 검사 결과를 반환
        } catch (Exception e) {
            return false;
        }
    }


    // 토큰에서 Email을 추출한다.
    public String getUid(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰에서 ROLE(권한)만 추출한다.
    public String getRole(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().get("role", String.class);
    }

}