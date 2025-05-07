package project.hotel_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

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
public class RevenueReportDTO {
    LocalDate date;

    BigDecimal totalRevenue;

    Integer bookingsCount;

    BigDecimal averageRevenue;
} 