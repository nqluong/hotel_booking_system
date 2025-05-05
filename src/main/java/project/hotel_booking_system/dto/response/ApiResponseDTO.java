package project.hotel_booking_system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {
    @Builder.Default
    int status = 200;
    
    @Builder.Default
    LocalDateTime time = LocalDateTime.now();
    
    boolean success;
    String message;
    T result;
    
    public ApiResponseDTO(boolean success, String message, T result) {
        this.success = success;
        this.message = message;
        this.result = result;
        this.status = success ? 200 : 400;
        this.time = LocalDateTime.now();
    }
}
