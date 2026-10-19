package com.attendance_system.repository;

import com.attendance_system.entity.Attendance;
import com.attendance_system.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudent(Student student);

    Optional<Attendance> findByStudent(Student student);
}
