package com.team4.usedTrade_app.user;

import com.team4.usedTrade_app.common.ConflictException;
import com.team4.usedTrade_app.user.dto.UserRequest;
import com.team4.usedTrade_app.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse signup(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname()
        );
    }
}
