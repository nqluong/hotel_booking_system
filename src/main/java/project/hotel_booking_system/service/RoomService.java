package project.hotel_booking_system.service;

import org.springframework.data.domain.Pageable;
import project.hotel_booking_system.dto.request.room_request.RoomCreationRequest;
import project.hotel_booking_system.dto.request.room_request.RoomUpdateRequest;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {

    RoomResponse getRoomByRoomNumber(String num);

    PaginationResponse<RoomResponse> getAllRoom(Pageable pageable);

    RoomResponse createRoom(RoomCreationRequest roomCreationRequest);

    RoomResponse updateRoom(String id, RoomUpdateRequest roomUpdateRequest);

    void deleteRoom(String id);

}
