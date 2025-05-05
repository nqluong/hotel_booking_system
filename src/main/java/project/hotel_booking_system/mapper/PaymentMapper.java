package project.hotel_booking_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import project.hotel_booking_system.dto.response.PaymentResponseDTO;
import project.hotel_booking_system.model.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    
    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "booking.room.roomNumber", target = "roomNumber")
    @Mapping(source = "booking.user.fullname", target = "userName")
    PaymentResponseDTO toDTO(Payment payment);
} 