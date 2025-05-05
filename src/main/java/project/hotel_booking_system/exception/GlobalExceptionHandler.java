package project.hotel_booking_system.exception;

import java.io.IOException;
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

import jakarta.servlet.http.HttpServletResponse;
import project.hotel_booking_system.dto.response.ApiResponseDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

    
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
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiResponseDTO<Object> handleGlobalException(
            Exception ex, WebRequest request) {
        
        return ApiResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .time(LocalDateTime.now())
                .message(ex.getMessage())
                .build();
    }
}
