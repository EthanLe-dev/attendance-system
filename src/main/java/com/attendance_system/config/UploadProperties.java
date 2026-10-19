package com.attendance_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "attendance.upload")
public class UploadProperties {

    private String selfieDirectory;
    private long maxFileSizeBytes;
    private long maxImagePixels;
}
