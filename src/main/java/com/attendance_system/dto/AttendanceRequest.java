package com.attendance_system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record AttendanceRequest(

        @NotBlank(message = "MSSV không được để trống")
        String mssv,

        @DecimalMin(value = "-90.0", message = "Latitude không hợp lệ")
        @DecimalMax(value = "90.0", message = "Latitude không hợp lệ")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude không hợp lệ")
        @DecimalMax(value = "180.0", message = "Longitude không hợp lệ")
        BigDecimal longitude,

        @DecimalMin(value = "0.0", inclusive = false, message = "Accuracy phải lớn hơn 0")
        BigDecimal accuracy,

        @NotNull(message = "Selfie không được để trống")
        MultipartFile selfie
) {
}
