package project.hotel_booking_system.dto.request.user_request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password;
    
    @Email(message = "Email should be valid")
    String email;
    
    String fullname;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    String phone;
} 