package project.hotel_booking_system.dto.request.room_request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlockDatesRequest {

    @NotNull(message = "Block dates cannot be null")
    @NotEmpty(message = "Block dates cannot be empty")
    List<LocalDate> blockDates;

    @NotNull(message = "Reason cannot be null")
    String reason;

}
