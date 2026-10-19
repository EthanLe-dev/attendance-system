package com.attendance_system.controller;

import com.attendance_system.dto.AttendanceSessionResponse;
import com.attendance_system.entity.AttendanceSession;
import com.attendance_system.service.AttendanceSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance-session")
@RequiredArgsConstructor
public class AttendanceSessionController {

    private final AttendanceSessionService attendanceSessionService;

    @PostMapping("/open")
    public ResponseEntity<AttendanceSessionResponse> openSession() {
        AttendanceSession session = attendanceSessionService.openSession();
        return ResponseEntity.ok(toResponse(session));
    }

    @PostMapping("/close")
    public ResponseEntity<AttendanceSessionResponse> closeSession() {
        AttendanceSession session = attendanceSessionService.closeSession();
        return ResponseEntity.ok(toResponse(session));
    }

    @GetMapping
    public ResponseEntity<AttendanceSessionResponse> getSession() {
        AttendanceSession session = attendanceSessionService.getSession();
        return ResponseEntity.ok(toResponse(session));
    }

    private AttendanceSessionResponse toResponse(AttendanceSession session) {
        return new AttendanceSessionResponse(
                session.getId(),
                session.getStatus(),
                session.getOpenedAt(),
                session.getClosedAt()
        );
    }
}
