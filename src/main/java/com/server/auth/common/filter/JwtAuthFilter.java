package com.server.auth.common.filter;

import com.server.auth.common.util.JwtUtil;
import com.server.auth.oauth.dto.UserInfoDto;
import com.server.auth.user.domain.User;
import com.server.auth.user.repository.UserRepository;
import com.server.auth.common.exception.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().contains("token/");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, IOException {
        // 헤더에서 토큰 가져오기
        String accessToken = parseBearerToken(request);

        log.info("JWT TOKEN FILTER IS RUNNING.... - token : {}", accessToken);

        if (!StringUtils.hasText(accessToken)) {
            doFilter(request, response, filterChain);
            return;
        }

        if (!jwtUtil.verifyToken(accessToken)) {
            throw new JwtException("Access Token 만료!");
        }

        if (jwtUtil.verifyToken(accessToken)) {

            // AccessToken 내부의 payload에 있는 email로 user를 조회한다
            User findUser = userRepository.findByUserEmail(jwtUtil.getUid(accessToken))
                    .orElseThrow(IllegalStateException::new);

            // SecurityContext에 등록할 User 객체를 만들어준다.
            UserInfoDto userDto = UserInfoDto.builder()
                    .userId(findUser.getUserId())
                    .email(findUser.getUserEmail())
                    .role(String.valueOf(findUser.getRole()))
                    .name(findUser.getUserName())
                    .build();

            // SecurityContext에 인증 객체를 등록해준다.
            Authentication auth = getAuthentication(userDto);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 받아온 토큰 값 앞에 붙어있는 'Bearer' 을 제거해주는 메서드
     * @param request - 순수 토큰 값
     */
    protected String parseBearerToken(
            HttpServletRequest request
    ) {

        log.info("pure token:{}", request.getHeader("Authorization"));

        // 요청 헤더에서 토큰 가져오기 - "Authorization" : "Bearer {token}"
        String bearerToken = request.getHeader("Authorization");

        log.info("bearerToken:{}", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public Authentication getAuthentication(UserInfoDto member) {
        return new UsernamePasswordAuthenticationToken(member, "",
                List.of(new SimpleGrantedAuthority(member.getRole())));
    }
}