package project.hotel_booking_system.service.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.InvalidatedTokenRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.service.payment.PaymentService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Value("${jwt.refreshable-duration}")
    private long refreshableDuration;

    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            Date expirationThreshold = Date.from(Instant.now().minusSeconds(refreshableDuration));
            long deletedCount = invalidatedTokenRepository.deleteByInvalidatedAtBefore(expirationThreshold);
            log.info("Cleaned up {} expired tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupTemporaryBookings() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(
                    BookingStatus.PENDING, cutoffTime);
            for( Booking booking : expiredBookings) {
                paymentRepository.deleteByBookingId(booking.getId());
                bookingRepository.delete(booking);
                log.info("Deleted temporary booking with ID: {}", booking.getId());
            }
        } catch (Exception e) {
            log.error("Error during temporary booking cleanup: {}", e.getMessage());
        }
    }
} 