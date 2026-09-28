package com.team4.usedTrade_app.user;

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

    @PostMapping

    public ResponseEntity<UserResponse> signup(
            @Valid @RequestBody UserRequest request) {

        UserResponse response = userService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}