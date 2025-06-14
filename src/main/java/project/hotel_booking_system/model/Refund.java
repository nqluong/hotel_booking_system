package project.hotel_booking_system.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    RefundStatus status;

    @Column(name = "refund_reason", length = 500)
    String refundReason;

    @Column(name = "vnpay_refund_id")
    String vnpayRefundId;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "processed_at")
    LocalDateTime processedAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
