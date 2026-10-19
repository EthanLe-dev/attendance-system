package com.attendance_system.dto;

public record StudentResponse(
        String mssv,
        String name,
        Integer groupNumber
) {
}
