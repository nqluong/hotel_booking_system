package project.hotel_booking_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import project.hotel_booking_system.dto.response.BookingResponseDTO;
import project.hotel_booking_system.model.Booking;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullname", target = "userName")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.roomNumber", target = "roomNumber")
    BookingResponseDTO toDTO(Booking booking);

} 