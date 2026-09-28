package com.team4.usedTrade_app.auth;

import com.team4.usedTrade_app.auth.dto.TokenResponse;
import com.team4.usedTrade_app.user.User;
import com.team4.usedTrade_app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public TokenResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return new TokenResponse(
                tokenProvider.issueAccessToken(user.getId()),
                tokenProvider.issueRefreshToken(user.getId()),
                user.getId(),
                user.getNickname()
        );
    }
}