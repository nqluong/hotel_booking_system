package project.hotel_booking_system.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.model.RoomImage;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomImageMapper {

    RoomImageResponse toImageResponse(RoomImage roomImage);
    List<RoomImageResponse> toRoomImageResponseList(List<RoomImage> roomImages);
}
