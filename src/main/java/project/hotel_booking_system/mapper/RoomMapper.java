package project.hotel_booking_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import project.hotel_booking_system.dto.request.room_request.RoomCreationRequest;
import project.hotel_booking_system.dto.request.room_request.RoomUpdateRequest;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.dto.response.RoomResponse;
import project.hotel_booking_system.model.Room;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper  {

    Room toRoom(RoomCreationRequest roomCreationRequest);

    @Mapping(target = "createdAt", source = "createAt")
    @Mapping(target = "images", ignore = true)
    RoomResponse toRoomResponse(Room room);

    void updateRoom(@MappingTarget Room room, RoomUpdateRequest roomUpdateRequest);

    default RoomResponse toRoomResponse(Room room, List<RoomImageResponse> images){
        RoomResponse response = toRoomResponse(room);
        response.setImages(images);
        return response;
    }

}
