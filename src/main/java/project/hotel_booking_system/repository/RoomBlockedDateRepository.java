package project.hotel_booking_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.hotel_booking_system.model.RoomBlockedDate;

import java.time.LocalDate;
import java.util.List;

public interface RoomBlockedDateRepository extends JpaRepository<RoomBlockedDate, Long> {

    List<RoomBlockedDate> findByRoomIdAndBlockedDateBetween(Long roomId, LocalDate startDate, LocalDate endDate);

    List<RoomBlockedDate> findByBlockedDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByRoomIdAndBlockedDate(Long roomId, LocalDate blockedDate);

    void deleteByRoomIdAndBlockedDateIn(Long roomId, List<LocalDate> dates);

    @Query("SELECT rbd FROM RoomBlockedDate rbd WHERE rbd.room.id = :roomId AND rbd.blockedDate >= :startDate " +
            "ORDER BY rbd.blockedDate")
    List<RoomBlockedDate> findFutureBlockedDatesByRoom(@Param("roomId") Long roomId,
                                                       @Param("startDate") LocalDate startDate);


    Page<RoomBlockedDate> findByRoomIdAndBlockedDateBetweenOrderByBlockedDate(
            Long roomId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
