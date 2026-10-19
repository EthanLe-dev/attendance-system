package com.attendance_system.service;

import com.attendance_system.entity.AttendanceSessionStatus;
import com.attendance_system.entity.AttendanceSession;
import com.attendance_system.repository.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceSessionService {

    private final AttendanceSessionRepository attendanceSessionRepository;

    public AttendanceSession openSession() {

        AttendanceSession session = attendanceSessionRepository
                .findById(1L)
                .orElseThrow(() -> new IllegalStateException("Attendance session not found"));

        session.setStatus(AttendanceSessionStatus.OPEN);
        session.setOpenedAt(LocalDateTime.now());
        session.setClosedAt(null);

        return attendanceSessionRepository.save(session);
    }

    public AttendanceSession closeSession() {

        AttendanceSession session = attendanceSessionRepository
                .findById(1L)
                .orElseThrow(() -> new IllegalStateException("Attendance session not found"));

        session.setStatus(AttendanceSessionStatus.CLOSED);
        session.setClosedAt(LocalDateTime.now());

        return attendanceSessionRepository.save(session);
    }

    public AttendanceSession getSession() {

        return attendanceSessionRepository
                .findById(1L)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Attendance session not found"
                        )
                );
    }
}
