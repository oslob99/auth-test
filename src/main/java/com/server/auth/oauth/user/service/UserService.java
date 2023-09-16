package com.server.auth.oauth.user.service;

import com.server.auth.oauth.user.domain.User;
import com.server.auth.oauth.user.dto.request.UserSignUpDto;
import com.server.auth.oauth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByUserEmail(email);
    }

    @Transactional
    public User save(UserSignUpDto registerDto) {
        // 이메일로 회원을 조회해서 이미 있다면 예외발생
        userRepository.findByUserEmail(registerDto.getEmail())
                .ifPresent(member -> {throw new IllegalArgumentException("이미 존재하는 회원입니다.");});

        User user = registerDto.toEntity();



        // 회원을 저장한다.
        User savedUser = userRepository.save(user);


        return savedUser;
    }


}



