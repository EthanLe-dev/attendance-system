package com.attendance_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "attendance.location")
@Getter
@Setter
public class LocationProperties {

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal radiusMeters;
    private BigDecimal maxAccuracyMeters;
}