package com.attendance_system.dto;

import com.attendance_system.entity.AttendanceSessionStatus;

import java.time.LocalDateTime;

public record AttendanceSessionResponse(
        Long id,
        AttendanceSessionStatus status,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
}
