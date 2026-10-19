package com.attendance_system.exception;

public class StudentAlreadyAttendedException extends RuntimeException {
    public StudentAlreadyAttendedException(String message) {
        super(message);
    }
}
