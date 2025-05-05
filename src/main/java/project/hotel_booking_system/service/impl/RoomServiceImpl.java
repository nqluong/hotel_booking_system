package project.hotel_booking_system.service.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import project.hotel_booking_system.dto.request.room_request.RoomCreationRequest;
import project.hotel_booking_system.dto.request.room_request.RoomUpdateRequest;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.dto.response.RoomResponse;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.RoomImageMapper;
import project.hotel_booking_system.mapper.RoomMapper;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.repository.RoomImageRepository;
import project.hotel_booking_system.repository.RoomRepository;
import project.hotel_booking_system.service.RoomService;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomServiceImpl implements RoomService {

    RoomRepository roomRepository;
    RoomMapper roomMapper;
    RoomImageMapper roomImageMapper;
    RoomImageRepository roomImageRepository;

    @Override
    public RoomResponse getRoomByRoomNumber(String num) {
        Room room =roomRepository.findByRoomNumber(num).orElseThrow(
                () ->  new AppException(ErrorCode.ROOM_NOT_FOUND)
        );

        List<RoomImageResponse> images = roomImageRepository.findByRoom_Id(room.getId())
                .stream()
                .map(roomImageMapper::toImageResponse)
                .toList();

        return roomMapper.toRoomResponse(room, images);

    }

    @Override
    public PaginationResponse<RoomResponse> getAllRoom(Pageable pageable) {
        Page<Room> rooms = roomRepository.findAll(pageable);

        List<RoomResponse> roomResponses = rooms.getContent()
                .stream()
                .map(roomMapper::toRoomResponse)
                .toList();

        return PaginationResponse.<RoomResponse>builder()
                .content(roomResponses)
                .currentPage(rooms.getNumber())
                .totalPages(rooms.getTotalPages())
                .totalElements(rooms.getTotalElements())
                .pageSize(rooms.getSize())
                .build();

    }

    @Override
    public RoomResponse createRoom(RoomCreationRequest roomCreationRequest) {
        roomRepository.findByRoomNumber(roomCreationRequest.getRoomNumber()).ifPresent(
                room -> {throw new AppException(ErrorCode.ROOM_EXISTED);
                });
        if(!EnumSet.allOf(RoomStatus.class).contains(roomCreationRequest.getRoomStatus())){
//            String validStatuses = EnumSet.allOf(RoomStatus.class).stream()
//                    .map(Enum::name)
//                    .collect(Collectors.joining(", "));
            throw new AppException(ErrorCode.INVALID_ROOM_STATUS);

        }

        Room room = roomMapper.toRoom(roomCreationRequest);
        room.setCreateAt(LocalDateTime.now());
        roomRepository.save(room);
        return roomMapper.toRoomResponse(room);
    }

    @Override
    public RoomResponse updateRoom(String num, RoomUpdateRequest roomUpdateRequest) {
        Room room = roomRepository.findByRoomNumber(num).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND)
        );
        if(!EnumSet.allOf(RoomStatus.class).contains(roomUpdateRequest.getRoomStatus())){

           throw new AppException(ErrorCode.INVALID_ROOM_STATUS);

        }
        roomMapper.updateRoom(room, roomUpdateRequest);

        roomRepository.save(room);
        return roomMapper.toRoomResponse(room);
    }

    @Override
    public void deleteRoom(String num) {
        Room room = roomRepository.findByRoomNumber(num).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND)
        );
        roomRepository.delete(room);
    }
}
