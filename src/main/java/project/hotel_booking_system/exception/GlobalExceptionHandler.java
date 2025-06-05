package project.hotel_booking_system.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ApiResponseDTO<?> handleAuthorizationDeniedException(AuthorizationDeniedException ex, HttpServletRequest request) {
        log.error("Authorization denied for request: {}", request.getRequestURI(), ex);
        return ApiResponseDTO.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("Access denied. You don't have the required role to access this resource.")
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponseDTO<?> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied for request: {}", request.getRequestURI(), ex);
        return ApiResponseDTO.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("Access denied. You don't have the required role to access this resource.")
                .build();
    }

    @ExceptionHandler(AppException.class)
    public ApiResponseDTO<?> handleAppException(AppException ex) {
        log.error("An application error occurred: {}", ex.getMessage(), ex);
        return ApiResponseDTO.builder()
                .status(ex.getErrorCode().getHttpStatusCode().value())
                .time(LocalDateTime.now())
                .success(false)
                .message(ex.getErrorCode().getMessage())
                .build();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponseDTO<?> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        log.error("No handler found for request: {}", request.getRequestURI(), ex);
        return ApiResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("The requested resource was not found")
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ApiResponseDTO<?> handleException(Exception ex, HttpServletRequest request) {
        log.error("An error occurred: {}", ex.getMessage(), ex);
        return ApiResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("An unexpected error occurred")
                .build();
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ApiResponseDTO<?> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        log.error("Resource already exists: {}", ex.getMessage(), ex);
        return ApiResponseDTO.builder()
                .status(HttpStatus.CONFLICT.value())
                .time(LocalDateTime.now())
                .success(false)
                .message(ex.getMessage())
                .result(null)
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiResponseDTO<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ApiResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .time(LocalDateTime.now())
                .success(false)
                .message(ex.getMessage())
                .result(null)
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDTO<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
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

        return ApiResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .time(LocalDateTime.now())
                .success(false)
                .message(message)
                .result(null)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResponseDTO<List<String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return ApiResponseDTO.<List<String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .time(LocalDateTime.now())
                .message("Validation error")
                .result(errors)
                .build();
    }

}
