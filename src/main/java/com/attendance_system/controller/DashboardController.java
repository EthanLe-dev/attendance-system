package com.attendance_system.controller;

import com.attendance_system.config.DashboardAuthInterceptor;
import com.attendance_system.dto.DashboardResponse;
import com.attendance_system.service.AttendanceService;
import com.attendance_system.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(
                dashboardService.getDashboard()
        );
    }

    @PostMapping("/manual-checkin/{mssv}")
    public ResponseEntity<Void> manualCheckIn(@PathVariable String mssv) {
        attendanceService.manualCheckIn(mssv);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-pin")
    public ResponseEntity<Void> verifyPin(HttpServletRequest request) {
        request.getSession(true).setAttribute(DashboardAuthInterceptor.AUTHENTICATED_SESSION_ATTRIBUTE, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(DashboardAuthInterceptor.AUTHENTICATED_SESSION_ATTRIBUTE);
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/selfies/{attendanceId}")
    public ResponseEntity<FileSystemResource> getSelfie(@PathVariable Long attendanceId) {
        var selfiePath = attendanceService.getSelfie(attendanceId);
        MediaType contentType = selfiePath.toString().endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(contentType)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(selfiePath));
    }
}
