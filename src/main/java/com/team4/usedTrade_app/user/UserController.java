package com.team4.usedTrade_app.user;

import com.team4.usedTrade_app.auth.AuthService;
import com.team4.usedTrade_app.auth.dto.LoginRequest;
import com.team4.usedTrade_app.auth.dto.TokenResponse;
import com.team4.usedTrade_app.user.dto.UserRequest;
import com.team4.usedTrade_app.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<UserResponse> signup(
            @Valid @RequestBody UserRequest request) {

        UserResponse response = userService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public TokenResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(
                request.email(),
                request.password()
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() { return ResponseEntity.noContent().build(); } }

}