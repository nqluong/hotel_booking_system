package project.hotel_booking_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // General errors
    UNCATEGORIZED_EXCEPTION("Unknown error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("Validation failed", HttpStatus.BAD_REQUEST),

    // User related error codes
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("User already exists", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("Username already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("Email already exists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("Account has been disabled", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED("Account has been locked", HttpStatus.FORBIDDEN),

    // Authentication & Authorization
    UNAUTHENTICATED("Authentication required", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("You do not have permission", HttpStatus.FORBIDDEN),
    ACCESS_DENIED("Access denied. Insufficient privileges", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED("Authentication token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("Invalid authentication token", HttpStatus.UNAUTHORIZED),

    // Room related
    ROOM_EXISTED("Room already exists", HttpStatus.CONFLICT),
    ROOM_NOT_FOUND("Room not found", HttpStatus.NOT_FOUND),
    INVALID_ROOM_STATUS("Invalid room status", HttpStatus.BAD_REQUEST),

    // Service related
    SERVICE_EXISTED("Service already exists", HttpStatus.CONFLICT),
    SERVICE_NOT_FOUND("Service not found", HttpStatus.NOT_FOUND),

    // Employee related
    EMPLOYEE_EXISTED("Employee already exists", HttpStatus.CONFLICT),
    EMPLOYEE_NOT_FOUND("Employee not found", HttpStatus.NOT_FOUND),

    // Check-in related
    CHECKIN_NOT_FOUND("Check-in record not found", HttpStatus.NOT_FOUND),
    CUSTOMER_NOT_FOUND("Customer not found", HttpStatus.NOT_FOUND),

    // Image related
    IMAGE_NOT_FOUND("Image not found", HttpStatus.NOT_FOUND),
    IMAGE_UPLOAD_FAILED("Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR),

    // Booking related
    BOOKING_NOT_FOUND("Booking not found", HttpStatus.NOT_FOUND),
    INVALID_BOOKING_STATUS_TRANSITION("Invalid booking status transition", HttpStatus.BAD_REQUEST),
    COMPLETED_BOOKING_UPDATE("Cannot update a completed booking", HttpStatus.BAD_REQUEST),
    CANCELLED_BOOKING_UPDATE("Cannot update a cancelled booking", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_BOOKING("Cannot cancel a completed booking", HttpStatus.BAD_REQUEST),
    INVALID_BOOKING_DATA("Invalid booking data", HttpStatus.BAD_REQUEST),
    ROOM_NOT_AVAILABLE("Room not available for the selected dates", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("Invalid date range provided", HttpStatus.BAD_REQUEST),
    EARLY_CHECK_IN("Cannot check-in before the reserved check-in date", HttpStatus.BAD_REQUEST),
    INVALID_CHECK_OUT("Cannot check-out before check-in date", HttpStatus.BAD_REQUEST),

    // Payment related
    PAYMENT_NOT_FOUND("Payment not found", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_STATUS_TRANSITION("Invalid payment status transition", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED("Payment has already been processed", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_AMOUNT("Invalid payment amount", HttpStatus.BAD_REQUEST),
    PAYMENT_REQUIRED("Payment is required to complete this operation", HttpStatus.BAD_REQUEST),
    INCOMPLETE_PAYMENT("Full payment is required to complete checkout", HttpStatus.BAD_REQUEST),
    CASH_PAYMENT_REQUIRED("Cash payment confirmation is required", HttpStatus.BAD_REQUEST);

    private String message;
    private HttpStatusCode httpStatusCode;

    ErrorCode(String message, HttpStatusCode httpStatusCode){
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

}
