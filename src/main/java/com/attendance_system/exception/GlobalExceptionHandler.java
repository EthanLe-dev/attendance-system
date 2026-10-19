package com.attendance_system.exception;

import com.attendance_system.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudentNotFound(StudentNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "STUDENT_NOT_FOUND",
                "Không tìm thấy sinh viên với MSSV này."
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(StudentAlreadyAttendedException.class)
    public ResponseEntity<ErrorResponse> handleStudentAlreadyAttended(StudentAlreadyAttendedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "STUDENT_ALREADY_ATTENDED",
                ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : "Sinh viên đã điểm danh trước đó."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(AttendanceSessionClosedException.class)
    public ResponseEntity<ErrorResponse> handleAttendanceSessionClosed(AttendanceSessionClosedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "ATTENDANCE_SESSION_CLOSED",
                "Hiện tại không trong thời gian điểm danh."
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .findFirst()
                .orElse("Dữ liệu không hợp lệ.");

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(LocationOutsideRadiusException.class)
    public ResponseEntity<ErrorResponse> handleLocationOutsideRadius(LocationOutsideRadiusException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "LOCATION_OUTSIDE_RADIUS",
                "Bạn đang ở ngoài khu vực điểm danh."
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(LocationAccuracyInsufficientException.class)
    public ResponseEntity<ErrorResponse> handleLocationAccuracyInsufficient(
            LocationAccuracyInsufficientException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        "LOCATION_ACCURACY_INSUFFICIENT",
                        "Độ chính xác GPS không đủ để điểm danh."
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_REQUEST", ex.getMessage()));
    }
}
