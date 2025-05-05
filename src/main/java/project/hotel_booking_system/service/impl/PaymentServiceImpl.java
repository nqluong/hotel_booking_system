package project.hotel_booking_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.hotel_booking_system.dto.request.payment_request.PaymentStatusUpdateDTO;
import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.PaymentMapper;
import project.hotel_booking_system.model.Payment;
import project.hotel_booking_system.repository.PaymentRepository;
import project.hotel_booking_system.service.PaymentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public List<PaymentResponseDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return paymentMapper.toDTO(payment);
    }

    @Override
    public PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatusUpdateDTO statusUpdateDTO) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        
        validateStatusTransition(payment.getStatus(), statusUpdateDTO.getStatus());
        
        payment.setStatus(statusUpdateDTO.getStatus());
        Payment updatedPayment = paymentRepository.save(payment);
        
        return paymentMapper.toDTO(updatedPayment);
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        //Kiểm tra trạng thái truyển đồi có hợp lệ hay không
        switch (currentStatus) {
            case PENDING:
                if (newStatus != PaymentStatus.PAID && newStatus != PaymentStatus.FAILED) {
                    throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS_TRANSITION);
                }
                break;
            case PAID:
            case FAILED:
                throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
            default:
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
} 