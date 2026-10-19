package com.attendance_system.exception;

public class AttendanceSessionClosedException extends RuntimeException {
    public AttendanceSessionClosedException(String message) {
        super(message);
    }
}
