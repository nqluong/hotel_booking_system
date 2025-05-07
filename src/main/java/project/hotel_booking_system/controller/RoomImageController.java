package project.hotel_booking_system.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.enums.ImageType;
import project.hotel_booking_system.service.impl.RoomImageServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/roomimages")
@RequiredArgsConstructor
public class RoomImageController {
    private final RoomImageServiceImpl roomImageService;

    @PostMapping("/upload/{number}")
    ApiResponseDTO<RoomImageResponse> uploadImage(@PathVariable("number") Long number,
                                                  @RequestParam("file") MultipartFile file,
                                                  @RequestParam("imageType")ImageType imageType){


        return ApiResponseDTO.<RoomImageResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .message("Them moi anh thanh cong")
                .result(roomImageService.uploadRoomImage(number,file,imageType))
                .build();


    }

    @GetMapping("/{number}")
    ApiResponseDTO<List<RoomImageResponse>> getImagesByRoomNumber(@PathVariable("number") Long number){
        return ApiResponseDTO.<List<RoomImageResponse>>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .result(roomImageService.getAllImagesByRoomNumber(number))
                .build();
    }

    @DeleteMapping("/{number}/{imageId}")
    ApiResponseDTO<String> deleteRoomImage(@PathVariable Long number,
                                           @PathVariable Long imageId){
        roomImageService.deleteImageByRoomNumber(number,imageId);
        return ApiResponseDTO.<String>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.NO_CONTENT.value())
                .result("Image deleted successfully")
                .build();
    }

    @PutMapping("/{number}/{id}")
    ApiResponseDTO<RoomImageResponse> updateImage(@PathVariable Long number,
                                                  @PathVariable Long id,
                                                  @RequestParam("file") MultipartFile file){
        return ApiResponseDTO.<RoomImageResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .result(roomImageService.updateRoomImage(number,id,file))
                .build();
    }

    @PutMapping("/{number}/{id}/type")
    ApiResponseDTO<RoomImageResponse> updateImageType(@PathVariable Long number,
                                                      @PathVariable Long id,
                                                      @RequestParam("type") ImageType type){
        return ApiResponseDTO.<RoomImageResponse>builder()
                .time(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .result(roomImageService.updateImageType(number,id,type))
                .build();
    }

}
