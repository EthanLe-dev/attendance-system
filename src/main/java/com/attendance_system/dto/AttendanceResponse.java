package com.attendance_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        String mssv,
        String name,
        Integer groupNumber,
        LocalDateTime checkedInAt,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracy,
        String selfieUrl,
        Boolean gpsBypassed,
        Boolean isManual
) {}