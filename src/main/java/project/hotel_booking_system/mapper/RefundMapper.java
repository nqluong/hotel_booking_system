package project.hotel_booking_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.hotel_booking_system.dto.response.RefundResponseDTO;
import project.hotel_booking_system.model.Refund;

@Mapper(componentModel = "spring")
public interface RefundMapper {
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "bookingId", source = "booking.id")
    RefundResponseDTO toDTO(Refund refund);
}
