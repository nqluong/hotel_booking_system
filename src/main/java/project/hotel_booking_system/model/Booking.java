package project.hotel_booking_system.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @Column(name = "check_in_date", nullable = false)
    Date checkInDate;

    @Column(name = "check_out_date", nullable = false)
    Date checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    BookingStatus status;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    BigDecimal totalPrice;

    @Column(name = "created_at",updatable = false, nullable = false)
    LocalDateTime createdAt;

}
