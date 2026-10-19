package com.attendance_system.dto;

public record ErrorResponse(
        String code,
        String message
) {
}
