package com.attendance_system.dto;

import com.attendance_system.entity.AttendanceSessionStatus;

import java.util.List;

public record DashboardResponse(
        long totalStudents,
        long attendedStudents,
        int attendancePercent,
        AttendanceSessionStatus sessionStatus,
        List<AttendanceResponse> attendances,
        List<StudentResponse> unattendedStudents
) {
}
