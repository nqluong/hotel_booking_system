package project.hotel_booking_system.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByStatus(PaymentStatus status);
    
    Page<Payment> findByBookingId(Long bookingId, Pageable pageable);
    
    // Phương thức tìm danh sách payment theo booking_id không phân trang
    List<Payment> findByBookingId(Long bookingId);
}
