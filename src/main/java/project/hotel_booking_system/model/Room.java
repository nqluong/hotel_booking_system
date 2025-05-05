package project.hotel_booking_system.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.enums.RoomType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "room_number", nullable = false,unique = true, length = 20)
    String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    RoomType roomType;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    RoomStatus roomStatus;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    String description;

    @Setter
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    List<RoomImage> images;

}
