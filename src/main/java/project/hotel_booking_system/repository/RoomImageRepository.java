package project.hotel_booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.model.RoomImage;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoom(Room room);

    List<RoomImage> findByRoom_Id(Long id);

    Optional<RoomImage> findByIdAndRoom(Long id, Room room);
}
