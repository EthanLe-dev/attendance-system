package com.attendance_system.service;

import com.attendance_system.dto.AttendanceResponse;
import com.attendance_system.dto.DashboardResponse;
import com.attendance_system.dto.StudentResponse;
import com.attendance_system.entity.Attendance;
import com.attendance_system.entity.AttendanceSession;
import com.attendance_system.entity.Student;
import com.attendance_system.repository.AttendanceRepository;
import com.attendance_system.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionService attendanceSessionService;

    public DashboardResponse getDashboard() {

        List<Student> allStudents = studentRepository.findAll();
        long totalStudents = allStudents.size();

        List<Attendance> attendanceEntities = attendanceRepository.findAll();

        List<AttendanceResponse> attendances = attendanceEntities
                .stream()
                .map(this::toAttendanceResponse)
                .toList();

        long attendedStudents = attendances.size();

        Set<String> attendedMssvSet = attendanceEntities.stream()
                .map(a -> a.getStudent().getMssv())
                .collect(Collectors.toSet());

        List<StudentResponse> unattendedStudents = allStudents.stream()
                .filter(student -> !attendedMssvSet.contains(student.getMssv()))
                .sorted(Comparator.comparing(Student::getGroupNumber, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Student::getMssv))
                .map(student -> new StudentResponse(
                        student.getMssv(),
                        student.getName(),
                        student.getGroupNumber()
                ))
                .toList();

        AttendanceSession session = attendanceSessionService.getSession();

        int attendancePercent = totalStudents > 0
                ? (int) Math.round(((double) attendedStudents / totalStudents) * 100)
                : 0;

        return new DashboardResponse(
                totalStudents,
                attendedStudents,
                attendancePercent,
                session.getStatus(),
                attendances,
                unattendedStudents
        );
    }

    private AttendanceResponse toAttendanceResponse(
            Attendance attendance
    ) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getStudent().getMssv(),
                attendance.getStudent().getName(),

                attendance.getStudent().getGroupNumber(),
                attendance.getCheckedInAt(),
                attendance.getLatitude(),
                attendance.getLongitude(),
                attendance.getAccuracy(),
                attendance.getSelfiePath() != null ? "/api/dashboard/selfies/" + attendance.getId() : null,
                attendance.isGpsBypassed(),
                attendance.isManual()
        );
    }
}
