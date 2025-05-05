package project.hotel_booking_system.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import project.hotel_booking_system.dto.request.room_request.RoomCreationRequest;
import project.hotel_booking_system.dto.request.room_request.RoomUpdateRequest;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.RoomResponse;
import project.hotel_booking_system.service.RoomService;

import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    ApiResponseDTO<RoomResponse> createRoom(@RequestBody RoomCreationRequest request){
        return ApiResponseDTO.<RoomResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .result(roomService.createRoom(request))
                .build();
    }

    @GetMapping
    ApiResponseDTO<PaginationResponse<RoomResponse>> getAllRoom(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page,size);
        PaginationResponse<RoomResponse> paginationResponse = roomService.getAllRoom(pageable);
        return ApiResponseDTO.<PaginationResponse<RoomResponse>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .result(paginationResponse)
                .build();
    }

    @GetMapping("/{number}")
    ApiResponseDTO<RoomResponse> getByRoomNumber(@PathVariable("number") String num){
        return ApiResponseDTO.<RoomResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .result(roomService.getRoomByRoomNumber(num))
                .build();
    }

    @PutMapping("/{number}")
    ApiResponseDTO<RoomResponse> updateRoom(@PathVariable("number") String num,
                                            @RequestBody RoomUpdateRequest request){
        return ApiResponseDTO.<RoomResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .result(roomService.updateRoom(num, request))
                .build();
    }

    @DeleteMapping("/{number}")
    ApiResponseDTO<String> deleteRoom(@PathVariable("number") String num){
        roomService.deleteRoom(num);
        return ApiResponseDTO.<String>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.NO_CONTENT.value())
                .result("Room has been deleted")
                .build();
    }

}
