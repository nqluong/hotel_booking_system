package project.hotel_booking_system.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import project.hotel_booking_system.dto.response.ApiResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage(), ex);

        ApiResponseDTO<Object> response = ApiResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .time(LocalDateTime.now())
                .success(false)
                .message(ex.getMessage())
                .result(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        log.error("Resource already exists: {}", ex.getMessage(), ex);

        ApiResponseDTO<Object> response = ApiResponseDTO.builder()
                .status(HttpStatus.CONFLICT.value())
                .time(LocalDateTime.now())
                .success(false)
                .message(ex.getMessage())
                .result(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleAppException(AppException ex) {
        log.error("Application error: {}", ex.getMessage(), ex);

        ApiResponseDTO<Object> response = ApiResponseDTO.builder()
                .status(ex.getErrorCode().getHttpStatusCode().value())
                .time(LocalDateTime.now())
                .success(false)
                .message(ex.getErrorCode().getMessage())
                .build();

        return ResponseEntity.status(ex.getErrorCode().getHttpStatusCode()).body(response);
    }

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponseDTO<Object>> handleAccessDeniedException(Exception ex, HttpServletRequest request) {
        log.error("Access denied for request: {}", request.getRequestURI(), ex);

        ApiResponseDTO<Object> response = ApiResponseDTO.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("Access denied. You don't have the required permissions to access this resource.")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage(), ex);

        String message = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;


        if ("No authenticated user found".equals(message)) {
            message = "Unauthorized - Please login to access this resource";
            status = HttpStatus.UNAUTHORIZED;
        }

        ApiResponseDTO<Object> response = ApiResponseDTO.builder()
                .status(status.value())
                .time(LocalDateTime.now())
                .success(false)
                .message(message)
                .result(null)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<List<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ApiResponseDTO<List<String>> response = ApiResponseDTO.<List<String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("Validation error")
                .result(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMissingParams(
            MissingServletRequestParameterException ex) {

        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Missing required parameter: " + ex.getParameterName())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage());

        String message;
        if (ex.getParameter().getParameterType().equals(LocalDate.class)) {
            String dateValue = String.valueOf(ex.getValue());
            message = "Invalid date: '" + dateValue + "'. Please use a valid date in format YYYY-MM-DD";

            if (dateValue.contains("02-31") || dateValue.contains("-2-31")) {
                message = "Date '" + dateValue + "' is invalid. February never has 31 days.";
            }
            else if (dateValue.contains("02-30") || dateValue.contains("-2-30")) {
                message = "Date '" + dateValue + "' is invalid. February never has 30 days.";
            }
            else if ((dateValue.contains("04-31") || dateValue.contains("-4-31") ||
                    dateValue.contains("06-31") || dateValue.contains("-6-31") ||
                    dateValue.contains("09-31") || dateValue.contains("-9-31") ||
                    dateValue.contains("11-31") || dateValue.contains("-11-31"))) {
                message = "Date '" + dateValue + "' is invalid. This month only has 30 days.";
            }
        } else {
            message = "Invalid parameter: " + ex.getName() + " should be a valid " + ex.getRequiredType().getSimpleName();
        }

        ApiResponseDTO<Object> response = ApiResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .time(LocalDateTime.now())
                .success(false)
                .message(message)
                .result(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        log.error("No handler found for request: {}", request.getRequestURI(), ex);

        ApiResponseDTO<Object> response = ApiResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("The requested resource was not found")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred for request: {}", request.getRequestURI(), ex);

        // Log chi tiết exception để debug
        log.error("Exception type: {}", ex.getClass().getName());
        log.error("Exception message: {}", ex.getMessage());
        log.error("Stack trace: ", ex);

        ApiResponseDTO<Object> response = ApiResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("An unexpected error occurred")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}