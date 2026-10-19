package com.attendance_system.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "attendance.dashboard")
@Validated
@Getter
@Setter
public class DashboardProperties {

    @NotBlank(message = "ATTENDANCE_DASHBOARD_PIN must be configured")
    @Pattern(regexp = "\\d{10}", message = "ATTENDANCE_DASHBOARD_PIN must contain exactly 10 digits")
    private String pin;
}
