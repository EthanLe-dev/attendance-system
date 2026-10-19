package com.attendance_system.exception;

public class LocationOutsideRadiusException extends RuntimeException {
    public LocationOutsideRadiusException(String message) {
        super(message);
    }
}
