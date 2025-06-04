package project.hotel_booking_system.service;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import project.hotel_booking_system.dto.request.room_request.RoomCreationRequest;
import project.hotel_booking_system.dto.request.room_request.RoomSearchRequest;
import project.hotel_booking_system.dto.request.room_request.RoomUpdateRequest;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RoomResponse;

public interface RoomService {

    RoomResponse getRoomByRoomNumber(Long num);

    PaginationResponse<RoomResponse> getAllRoom(Pageable pageable);

    RoomResponse createRoom(RoomCreationRequest roomCreationRequest);

    RoomResponse updateRoom(Long id, RoomUpdateRequest roomUpdateRequest);

    void deleteRoom(Long id);

    PaginationResponse<RoomResponse> searchAvailableRooms(RoomSearchRequest searchRequest, Pageable pageable);

}
