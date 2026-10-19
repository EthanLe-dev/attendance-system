package com.attendance_system.service;

import com.attendance_system.entity.Student;
import com.attendance_system.exception.StudentNotFoundException;
import com.attendance_system.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public Student findByMssv(String mssv) {
        return studentRepository.findByMssv(mssv)
                .orElseThrow(() ->
                        new StudentNotFoundException(
                                "Student with MSSV " + mssv + " not found"
                        )
                );
    }
}
