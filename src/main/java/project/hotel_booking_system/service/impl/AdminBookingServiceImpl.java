package project.hotel_booking_system.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.hotel_booking_system.dto.request.booking_request.BookingStatusUpdateDTO;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.BookingMapper;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.service.AdminBookingService;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminBookingServiceImpl implements AdminBookingService {

    BookingRepository bookingRepository;
    BookingMapper bookingMapper;
    PaymentRepository paymentRepository;

    LocalTime STANDARD_CHECK_IN_TIME = LocalTime.of(14, 0);

    LocalTime STANDARD_CHECK_OUT_TIME = LocalTime.of(12, 0);

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();

        return bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
    }

    @Override
    public BookingResponseDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return bookingMapper.toDTO(booking);
    }

    @Override
    public BookingResponseDTO updateBookingStatus(Long id, BookingStatusUpdateDTO statusUpdateDTO) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        validateStatusTransition(booking, statusUpdateDTO.getStatus());

        booking.setStatus(statusUpdateDTO.getStatus());
        Booking updatedBooking = bookingRepository.save(booking);
        
        return bookingMapper.toDTO(updatedBooking);
    }

    @Override
    public List<BookingResponseDTO> getBookingsByStatus(BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
    }
    
    private void validateStatusTransition(Booking booking, BookingStatus newStatus) {
        BookingStatus currentStatus = booking.getStatus();
        
        switch (currentStatus) {
            case PENDING:
                if (newStatus != BookingStatus.CONFIRMED && newStatus != BookingStatus.CANCELLED) {
                    throw new AppException(ErrorCode.INVALID_BOOKING_STATUS_TRANSITION);
                }
                break;
            case CONFIRMED:
                if (newStatus != BookingStatus.CHECKED_IN && newStatus != BookingStatus.CANCELLED) {
                    throw new AppException(ErrorCode.INVALID_BOOKING_STATUS_TRANSITION);
                }
                if (newStatus == BookingStatus.CHECKED_IN) {
                    validateCheckInTime(booking);
                }
                break;
            case CHECKED_IN:
                if (newStatus != BookingStatus.COMPLETED) {
                    throw new AppException(ErrorCode.INVALID_BOOKING_STATUS_TRANSITION);
                }
                
                // Check if full payment has been made before allowing COMPLETED status
                validateFullPayment(booking);
                
                // Validate check-out time
                validateCheckOutTime(booking);
                break;
            case COMPLETED:
                throw new AppException(ErrorCode.COMPLETED_BOOKING_UPDATE);
            case CANCELLED:
                throw new AppException(ErrorCode.CANCELLED_BOOKING_UPDATE);
            default:
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    private void validateCheckInTime(Booking booking) {
        LocalDate checkInDate = convertToLocalDate(booking.getCheckInDate());
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (today.isBefore(checkInDate)) {
            throw new AppException(ErrorCode.EARLY_CHECK_IN);
        }

        if (today.isEqual(checkInDate) && now.isBefore(STANDARD_CHECK_IN_TIME)) {
            log.warn("Early check-in detected for booking ID: {}. Standard check-in time is {}",
                    booking.getId(), STANDARD_CHECK_IN_TIME);
        }
    }
    
    private void validateCheckOutTime(Booking booking) {
        LocalDate checkOutDate = convertToLocalDate(booking.getCheckOutDate());
        LocalDate checkInDate = convertToLocalDate(booking.getCheckInDate());
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (today.isBefore(checkInDate)) {
            throw new AppException(ErrorCode.INVALID_CHECK_OUT);
        }

        if (today.isAfter(checkOutDate)) {
            log.warn("Late check-out detected for booking ID: {}. Additional charges may apply.",
                    booking.getId());
        }

        if (today.isEqual(checkOutDate) && now.isAfter(STANDARD_CHECK_OUT_TIME)) {
            log.warn("Late check-out time for booking ID: {}. Standard check-out time is {}. Additional charges may apply.",
                    booking.getId(), STANDARD_CHECK_OUT_TIME);
        }
    }
    
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    private void validateFullPayment(Booking booking) {
        List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
        
        if (payments.isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_REQUIRED);
        }

        BigDecimal totalPaidAmount = payments.stream()
                .filter(payment -> PaymentStatus.COMPLETED.equals(payment.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Check if total paid amount equals booking total price
        if (totalPaidAmount.compareTo(booking.getTotalPrice()) < 0) {
            throw new AppException(ErrorCode.INCOMPLETE_PAYMENT);
        }
    }
} 