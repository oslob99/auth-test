package com.server.auth.oauth.refresh.service;

import com.server.auth.oauth.refresh.domain.RefreshToken;
import com.server.auth.oauth.refresh.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    /**
     * @param email 구글 로그인한 이메일
     * AT, RT 저장
     */
    @Transactional
    public void saveTokenInfo(String email, String refreshToken, String accessToken) {
        repository.save(new RefreshToken(email, accessToken, refreshToken));
    }

    /**
     *
     * @param accessToken AT로 RT식제
     */
    @Transactional
    public void removeRefreshToken(String accessToken) {
        RefreshToken token = repository.findByAccessToken(accessToken)
                .orElseThrow(IllegalArgumentException::new);

        repository.delete(token);
    }
}