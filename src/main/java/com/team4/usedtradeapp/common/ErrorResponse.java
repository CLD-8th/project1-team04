package com.team4.usedtradeapp.common;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String code,
        String message,
        LocalDateTime timestamp
) {
}
