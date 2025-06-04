package project.hotel_booking_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    ROOM_EXISTED("Room existed", HttpStatus.BAD_REQUEST),
    ROOM_NOT_FOUND("Room not found", HttpStatus.BAD_REQUEST),
    SERVICE_EXISTED("Service existed", HttpStatus.BAD_REQUEST),
    SERVICE_NOT_FOUND("Service not found", HttpStatus.BAD_REQUEST),
    EMPLOYEE_EXISTED("Employee existed", HttpStatus.BAD_REQUEST ),
    EMPLOYEE_NOT_FOUND("Employee not found", HttpStatus.BAD_REQUEST),
    CHECKIN_NOT_FOUND("Checkin not found", HttpStatus.BAD_REQUEST),
    CUSTOMER_NOT_FOUND("Customer not found", HttpStatus.BAD_REQUEST),
    INVALID_ROOM_STATUS("Invalid room status", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND("Image not found", HttpStatus.BAD_REQUEST),
    
    // Booking related error codes
    BOOKING_NOT_FOUND("Booking not found", HttpStatus.NOT_FOUND),
    INVALID_BOOKING_STATUS_TRANSITION("Invalid booking status transition", HttpStatus.BAD_REQUEST),
    COMPLETED_BOOKING_UPDATE("Cannot update a completed booking", HttpStatus.BAD_REQUEST),
    CANCELLED_BOOKING_UPDATE("Cannot update a cancelled booking", HttpStatus.BAD_REQUEST),
    INVALID_BOOKING_DATA("Invalid booking data", HttpStatus.BAD_REQUEST),
    ROOM_NOT_AVAILABLE("Room not available for the selected dates", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("Invalid date range provided", HttpStatus.BAD_REQUEST),
    EARLY_CHECK_IN("Cannot check-in before the reserved check-in date", HttpStatus.BAD_REQUEST),
    INVALID_CHECK_OUT("Cannot check-out before check-in date", HttpStatus.BAD_REQUEST),
    
    // Payment related error codes
    PAYMENT_NOT_FOUND("Payment not found", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_STATUS_TRANSITION("Invalid payment status transition", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED("Payment has already been processed", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_AMOUNT("Invalid payment amount", HttpStatus.BAD_REQUEST),
    PAYMENT_REQUIRED("Payment is required to complete this operation", HttpStatus.BAD_REQUEST),
    INCOMPLETE_PAYMENT("Full payment is required to complete checkout", HttpStatus.BAD_REQUEST),
    CASH_PAYMENT_REQUIRED("Cash payment confirmation is required", HttpStatus.BAD_REQUEST),

    //User related error codes
    USER_NOT_EXISTED("User does not exist", HttpStatus.BAD_REQUEST),
    USER_EXISTED("User already exists", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED("Unauthenticated", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("",HttpStatus.FORBIDDEN ),
    CANNOT_CANCEL_BOOKING("",HttpStatus.BAD_REQUEST );
    private String message;
    private HttpStatusCode httpStatusCode;

    ErrorCode(String message, HttpStatusCode httpStatusCode){
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

}
