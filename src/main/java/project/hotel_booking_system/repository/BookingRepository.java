package project.hotel_booking_system.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(BookingStatus status);
    
    Page<Booking> findByUser_Id(Long userId, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
           "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate) OR " +
           "(b.checkInDate >= :checkInDate AND b.checkInDate <= :checkOutDate) OR " +
           "(b.checkOutDate >= :checkInDate AND b.checkOutDate <= :checkOutDate)) " +
           "AND b.status != 'CANCELLED'")
    List<Booking> findOverlappingBookings(@Param("roomId") Long roomId, 
                                         @Param("checkInDate") Date checkInDate,
                                         @Param("checkOutDate") Date checkOutDate);
}
