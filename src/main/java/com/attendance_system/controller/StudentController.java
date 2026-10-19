package com.attendance_system.controller;

import com.attendance_system.dto.StudentResponse;
import com.attendance_system.entity.Student;
import com.attendance_system.exception.StudentAlreadyAttendedException;
import com.attendance_system.repository.AttendanceRepository;
import com.attendance_system.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final AttendanceRepository attendanceRepository;

    @GetMapping("/{mssv}")
    public ResponseEntity<StudentResponse> checkStudent(@PathVariable String mssv) {
        Student student = studentService.findByMssv(mssv);

        if (attendanceRepository.existsByStudent(student)) {
            throw new StudentAlreadyAttendedException(
                    "Sinh viên " + student.getName() + " (MSSV: " + mssv + ") đã được điểm danh trước đó rồi."
            );
        }

        StudentResponse response = new StudentResponse(
                student.getMssv(),
                student.getName(),
                student.getGroupNumber()
        );

        return ResponseEntity.ok(response);
    }
}
