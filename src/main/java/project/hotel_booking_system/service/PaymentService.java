package project.hotel_booking_system.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import project.hotel_booking_system.dto.request.payment_request.PaymentRequestDTO;
import project.hotel_booking_system.dto.request.payment_request.PaymentStatusUpdateDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentStatus;

public interface PaymentService {

    List<PaymentResponseDTO> getAllPayments();

    PaymentResponseDTO getPaymentById(Long id);

    PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatusUpdateDTO statusUpdateDTO);

    List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status);
    
    // Method for VNPay payment
    PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequestDTO);
    
    PaymentResponseDTO processVnPayPayment(PaymentRequestDTO paymentRequestDTO, String clientIp);
    
    PaymentResponseDTO handleVnPayCallback(String vnPayResponse);
    
    PaginationResponse<PaymentResponseDTO> getBookingPayments(Long bookingId, Pageable pageable);
} 