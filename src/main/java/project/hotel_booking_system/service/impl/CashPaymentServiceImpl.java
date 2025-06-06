package project.hotel_booking_system.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.payment_request.CashPaymentRequestDTO;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.PaymentMethod;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.model.Booking;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.BookingRepository;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.service.CashPaymentService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CashPaymentServiceImpl implements CashPaymentService {

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponseDTO processCashPayment(CashPaymentRequestDTO cashPaymentRequestDTO) {
        // Validate staff confirmation
        if (!cashPaymentRequestDTO.getStaffConfirmation()) {
            throw new AppException(ErrorCode.CASH_PAYMENT_REQUIRED);
        }
        
        Booking booking = bookingRepository.findById(cashPaymentRequestDTO.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        

        BigDecimal remainingAmount = calculateRemainingAmount(booking);
        

        if (cashPaymentRequestDTO.getAmount().compareTo(remainingAmount) > 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        Payment cashPayment = Payment.builder()
                .booking(booking)
                .amount(cashPaymentRequestDTO.getAmount())
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .retryCount(0)
                .build();
        
        Payment savedPayment = paymentRepository.save(cashPayment);
        

        BigDecimal totalPaidAmount = getTotalPaidAmount(booking.getId());
        if (totalPaidAmount.compareTo(booking.getTotalPrice()) >= 0 && 
                BookingStatus.CHECKED_IN.equals(booking.getStatus())) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        }
        
        return mapToPaymentResponseDTO(savedPayment);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public double getRemainingPaymentAmount(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        
        BigDecimal remainingAmount = calculateRemainingAmount(booking);
        return remainingAmount.doubleValue();
    }
    
    private BigDecimal calculateRemainingAmount(Booking booking) {
        BigDecimal totalPrice = booking.getTotalPrice();
        BigDecimal totalPaid = getTotalPaidAmount(booking.getId());
        
        return totalPrice.subtract(totalPaid);
    }
    
    private BigDecimal getTotalPaidAmount(Long bookingId) {
        List<Payment> payments = paymentRepository.findByBookingId(bookingId);
        
        return payments.stream()
                .filter(payment -> PaymentStatus.COMPLETED.equals(payment.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private PaymentResponseDTO mapToPaymentResponseDTO(Payment payment) {
        Booking booking = payment.getBooking();
        
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .bookingId(booking.getId())
                .roomNumber(booking.getRoom().getRoomNumber())
                .userName(booking.getUser().getUsername())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .build();
    }
} 