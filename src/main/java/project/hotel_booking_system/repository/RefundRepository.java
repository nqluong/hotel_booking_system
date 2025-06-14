package project.hotel_booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.hotel_booking_system.enums.RefundStatus;
import project.hotel_booking_system.model.Refund;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByBookingId(Long bookingId);

    List<Refund> findByPaymentId(Long paymentId);

    List<Refund> findByStatus(RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.booking.id = :bookingId AND r.status = :status")
    Optional<Refund> findByBookingIdAndStatus(@Param("bookingId") Long bookingId,
                                              @Param("status") RefundStatus status);
}
