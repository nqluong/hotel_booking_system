package project.hotel_booking_system.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.ImageType;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "roomimages")
public class RoomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @Column(name = "image_url", length = 255, nullable = false)
    String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 20, nullable = false)
    ImageType imageType;

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

}
