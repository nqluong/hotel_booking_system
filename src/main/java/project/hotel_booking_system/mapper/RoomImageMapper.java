package project.hotel_booking_system.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Value;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.model.RoomImage;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomImageMapper {

    @Value("${server.servlet.context-path}")
    String contextPath = "/hotelbooking";

    @Mapping(target = "fullImageUrl", expression = "java(getFullImageUrl(roomImage.getImageUrl()))")
    RoomImageResponse toImageResponse(RoomImage roomImage);
    
    List<RoomImageResponse> toRoomImageResponseList(List<RoomImage> roomImages);

    default String getFullImageUrl(String imageUrl) {
        return contextPath + "/uploads/" + imageUrl;
    }
}
