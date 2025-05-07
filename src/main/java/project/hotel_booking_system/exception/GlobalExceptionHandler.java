package project.hotel_booking_system.exception;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.response.ApiResponseDTO;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponseDTO<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        return ApiResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .time(LocalDateTime.now())
                .success(false)
                .message(ex.getMessage())
                .result(null)
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDTO<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ApiResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
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

            // Kiểm tra nếu là ngày 31 tháng 2
            if (dateValue.contains("02-31") || dateValue.contains("-2-31")) {
                message = "Date '" + dateValue + "' is invalid. February never has 31 days.";
            }
            // Kiểm tra nếu là ngày 30 tháng 2
            else if (dateValue.contains("02-30") || dateValue.contains("-2-30")) {
                message = "Date '" + dateValue + "' is invalid. February never has 30 days.";
            }
            // Kiểm tra các tháng 30 ngày
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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDTO<Object> handleGenericException(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ApiResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .time(LocalDateTime.now())
                .success(false)
                .message("An unexpected error occurred")
                .result(null)
                .build();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResponseDTO<Object> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        return ApiResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .time(LocalDateTime.now())
                .message(ex.getMessage())
                .build();
    }
    
    @ExceptionHandler(AppException.class)
    @ResponseBody
    public ApiResponseDTO<Object> handleAppException(
            AppException ex, WebRequest request, HttpServletResponse response) throws IOException {
        
        response.setStatus(ex.getErrorCode().getHttpStatusCode().value());
        
        return ApiResponseDTO.builder()
                .status(ex.getErrorCode().getHttpStatusCode().value())
                .time(LocalDateTime.now())
                .message(ex.getMessage())
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
