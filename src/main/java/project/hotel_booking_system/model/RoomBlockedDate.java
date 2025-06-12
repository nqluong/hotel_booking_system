package project.hotel_booking_system.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "room_blocked_dates")
public class RoomBlockedDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @Column(name = "blocked_date", nullable = false)
    LocalDate blockedDate;

    @Column(name = "reason", length = 255)
    String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    Long createdBy;
}
