package project.hotel_booking_system.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.enums.RoomType;
import project.hotel_booking_system.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String num);
    Page<Room> findAll(Pageable pageable);
    void deleteByRoomNumber(String num);
    
    // Find available rooms by room type
    Page<Room> findByRoomTypeAndRoomStatus(RoomType roomType, RoomStatus roomStatus, Pageable pageable);
    
    // Find available rooms by price range
    Page<Room> findByPriceBetweenAndRoomStatus(BigDecimal minPrice, BigDecimal maxPrice, RoomStatus roomStatus, Pageable pageable);
    
    // Find available rooms by room type and price range
    Page<Room> findByRoomTypeAndPriceBetweenAndRoomStatus(RoomType roomType, BigDecimal minPrice, BigDecimal maxPrice, RoomStatus roomStatus, Pageable pageable);
    
    // Query to find available rooms (not booked) for a specific date range
    @Query("SELECT r FROM Room r WHERE r.roomStatus = :status AND r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE " +
           "((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate) OR " +
           "(b.checkInDate >= :checkInDate AND b.checkInDate <= :checkOutDate) OR " +
           "(b.checkOutDate >= :checkInDate AND b.checkOutDate <= :checkOutDate)) AND " +
           "b.status != 'CANCELLED')")
    Page<Room> findAvailableRoomsForDateRange(@Param("checkInDate") Date checkInDate,
                                             @Param("checkOutDate") Date checkOutDate,
                                             @Param("status") RoomStatus status,
                                             Pageable pageable);
    
    // Query to find available rooms by date range, room type and price range
    @Query("SELECT r FROM Room r WHERE r.roomStatus = :status " +
           "AND (:roomType IS NULL OR r.roomType = :roomType) " +
           "AND (:minPrice IS NULL OR r.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR r.price <= :maxPrice) " +
           "AND r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE " +
           "((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate) OR " +
           "(b.checkInDate >= :checkInDate AND b.checkInDate <= :checkOutDate) OR " +
           "(b.checkOutDate >= :checkInDate AND b.checkOutDate <= :checkOutDate)) AND " +
           "b.status != 'CANCELLED')")
    Page<Room> searchAvailableRooms(@Param("checkInDate") Date checkInDate,
                                   @Param("checkOutDate") Date checkOutDate,
                                   @Param("roomType") RoomType roomType,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   @Param("status") RoomStatus status,
                                   Pageable pageable);
}
