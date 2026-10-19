package com.attendance_system;

import com.attendance_system.config.DashboardProperties;
import com.attendance_system.config.LocationProperties;
import com.attendance_system.config.UploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		LocationProperties.class,
		UploadProperties.class,
		DashboardProperties.class
})
public class AttendanceSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendanceSystemApplication.class, args);
	}

}
