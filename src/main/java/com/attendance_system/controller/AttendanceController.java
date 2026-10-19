package com.attendance_system.controller;

import com.attendance_system.dto.AttendanceRequest;
import com.attendance_system.dto.AttendanceResponse;
import com.attendance_system.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<Void> createAttendance(@Valid @ModelAttribute AttendanceRequest request) {
        attendanceService.createAttendance(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAllAttendances() {
        return ResponseEntity.ok(
                attendanceService.getAllAttendances()
        );
    }
}
