package project.hotel_booking_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.hotel_booking_system.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long roomId;
    private String roomNumber;
    private Date checkInDate;
    private Date checkOutDate;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
} 