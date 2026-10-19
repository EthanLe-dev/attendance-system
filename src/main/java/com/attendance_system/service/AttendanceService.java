package com.attendance_system.service;

import com.attendance_system.dto.AttendanceResponse;
import com.attendance_system.entity.Attendance;
import com.attendance_system.entity.AttendanceSessionStatus;
import com.attendance_system.dto.AttendanceRequest;
import com.attendance_system.entity.AttendanceSession;
import com.attendance_system.entity.Student;
import com.attendance_system.exception.AttendanceSessionClosedException;
import com.attendance_system.exception.LocationAccuracyInsufficientException;
import com.attendance_system.exception.LocationOutsideRadiusException;
import com.attendance_system.exception.StudentAlreadyAttendedException;
import com.attendance_system.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final StudentService studentService;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionService attendanceSessionService;
    private final LocationService locationService;
    private final SelfieStorageService selfieStorageService;

    public void createAttendance(AttendanceRequest request) {
        Student student = studentService.findByMssv(request.mssv());

        if (attendanceRepository.existsByStudent(student)) {
            throw new StudentAlreadyAttendedException("Student with MSSV " + request.mssv() + " has already attended");
        }

        AttendanceSession session = attendanceSessionService.getSession();

        if (session.getStatus() != AttendanceSessionStatus.OPEN) {
            throw new AttendanceSessionClosedException("Attendance session is currently closed");
        }

        if (request.latitude() == null || request.longitude() == null || request.accuracy() == null) {
            throw new IllegalArgumentException("Thông tin tọa độ GPS không đầy đủ");
        }

        boolean withinRadius = locationService.isWithinRadius(
                request.latitude(),
                request.longitude()
        );

        if (!withinRadius) {
            throw new LocationOutsideRadiusException("Student is outside attendance location");
        }

        boolean accuracyAcceptable = locationService.isAccuracyAcceptable(request.accuracy());

        if (!accuracyAcceptable) {
            throw new LocationAccuracyInsufficientException("GPS accuracy is insufficient");
        }

        String selfiePath =
                selfieStorageService.store(request.selfie());

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCheckedInAt(LocalDateTime.now());
        attendance.setLatitude(request.latitude());
        attendance.setLongitude(request.longitude());
        attendance.setAccuracy(request.accuracy());
        attendance.setSelfiePath(selfiePath);
        attendance.setGpsBypassed(false);
        attendance.setManual(false);

        try {
            attendanceRepository.saveAndFlush(attendance);
        } catch (DataIntegrityViolationException e) {
            selfieStorageService.delete(selfiePath);

            throw new StudentAlreadyAttendedException(
                    "Student with MSSV " + request.mssv()
                            + " has already attended"
            );
        }
    }

    public void manualCheckIn(String mssv) {
        Student student = studentService.findByMssv(mssv);

        if (attendanceRepository.existsByStudent(student)) {
            throw new StudentAlreadyAttendedException("Sinh viên với MSSV " + mssv + " đã được điểm danh trước đó.");
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCheckedInAt(LocalDateTime.now());
        attendance.setLatitude(null);
        attendance.setLongitude(null);
        attendance.setAccuracy(null);
        attendance.setSelfiePath(null);
        attendance.setGpsBypassed(false);
        attendance.setManual(true);

        attendanceRepository.saveAndFlush(attendance);
    }

    public List<AttendanceResponse> getAllAttendances() {

        return attendanceRepository.findAll()
                .stream()
                .map(attendance -> new AttendanceResponse(
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
                ))
                .toList();
    }

    public Path getSelfie(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi điểm danh."));
        if (attendance.getSelfiePath() == null) {
            throw new IllegalArgumentException("Bản ghi này không có ảnh selfie.");
        }
        return selfieStorageService.load(attendance.getSelfiePath());
    }
}
