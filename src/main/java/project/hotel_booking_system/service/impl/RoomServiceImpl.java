package project.hotel_booking_system.service.impl;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.dto.request.room_request.RoomCreationRequest;
import project.hotel_booking_system.dto.request.room_request.RoomSearchRequest;
import project.hotel_booking_system.dto.request.room_request.RoomUpdateRequest;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.dto.response.RoomResponse;
import project.hotel_booking_system.enums.ImageType;
import project.hotel_booking_system.enums.RoomStatus;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.RoomImageMapper;
import project.hotel_booking_system.mapper.RoomMapper;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.repository.RoomImageRepository;
import project.hotel_booking_system.repository.RoomRepository;
import project.hotel_booking_system.service.RoomService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomServiceImpl implements RoomService {

    RoomRepository roomRepository;
    RoomMapper roomMapper;
    RoomImageMapper roomImageMapper;
    RoomImageRepository roomImageRepository;

    @Override
    public RoomResponse getRoomByRoomNumber(Long num) {
        Room room =roomRepository.findById(num).orElseThrow(
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
                .map(room -> {
                    List<RoomImageResponse> thumbnailImages = roomImageRepository.findByRoom_IdAndImageType(room.getId(), ImageType.THUMBNAIL)
                            .stream()
                            .map(roomImageMapper::toImageResponse)
                            .toList();
                    return roomMapper.toRoomResponse(room, thumbnailImages);
                })
                .toList();

        return PaginationResponse.<RoomResponse>builder()
                .content(roomResponses)
                .page(rooms.getNumber())
                .totalPages(rooms.getTotalPages())
                .totalElements(rooms.getTotalElements())
                .pageSize(rooms.getSize())
                .last(rooms.isLast())
                .build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse updateRoom(Long num, RoomUpdateRequest roomUpdateRequest) {
        Room room = roomRepository.findById(num).orElseThrow(
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
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRoom(Long num) {
        Room room = roomRepository.findById(num).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND)
        );
        roomRepository.delete(room);
    }
    
    @Override
    public PaginationResponse<RoomResponse> searchAvailableRooms(RoomSearchRequest searchRequest, Pageable pageable) {

        if (searchRequest.getCheckInDate() != null && searchRequest.getCheckOutDate() != null) {
            Date now = new Date();
            if (searchRequest.getCheckInDate().before(now)) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
            if (searchRequest.getCheckOutDate().before(searchRequest.getCheckInDate())) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
        }

        Page<Room> rooms;
        if (searchRequest.getCheckInDate() != null && searchRequest.getCheckOutDate() != null) {
            // If dates are provided, use the date-based search
            rooms = roomRepository.searchAvailableRooms(
                    searchRequest.getCheckInDate(),
                    searchRequest.getCheckOutDate(),
                    searchRequest.getRoomType(),
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    RoomStatus.AVAILABLE,
                    pageable
            );
        } else if (searchRequest.getRoomType() != null && searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
            // If only room type and price range are provided
            rooms = roomRepository.findByRoomTypeAndPriceBetweenAndRoomStatus(
                    searchRequest.getRoomType(),
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    RoomStatus.AVAILABLE,
                    pageable
            );
        } else if (searchRequest.getRoomType() != null) {
            // If only room type is provided
            rooms = roomRepository.findByRoomTypeAndRoomStatus(
                    searchRequest.getRoomType(),
                    RoomStatus.AVAILABLE,
                    pageable
            );
        } else if (searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
            // If only price range is provided
            rooms = roomRepository.findByPriceBetweenAndRoomStatus(
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    RoomStatus.AVAILABLE,
                    pageable
            );
        } else {
            // If no specific criteria, return all available rooms
            rooms = roomRepository.findAll(pageable);
        }
        
        // Map the results to DTOs with thumbnail images
        List<RoomResponse> roomResponses = rooms.getContent()
                .stream()
                .map(room -> {
                    List<RoomImageResponse> thumbnailImages = roomImageRepository.findByRoom_IdAndImageType(room.getId(), ImageType.THUMBNAIL)
                            .stream()
                            .map(roomImageMapper::toImageResponse)
                            .toList();
                    return roomMapper.toRoomResponse(room, thumbnailImages);
                })
                .toList();

        return PaginationResponse.<RoomResponse>builder()
                .content(roomResponses)
                .page(rooms.getNumber())
                .totalPages(rooms.getTotalPages())
                .totalElements(rooms.getTotalElements())
                .pageSize(rooms.getSize())
                .last(rooms.isLast())
                .build();
    }
}
