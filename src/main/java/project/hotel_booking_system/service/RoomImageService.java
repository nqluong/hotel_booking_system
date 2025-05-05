package project.hotel_booking_system.service;

import org.springframework.web.multipart.MultipartFile;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.enums.ImageType;

import java.io.IOException;
import java.util.List;

public interface RoomImageService {

    RoomImageResponse uploadRoomImage(String roomNumber, MultipartFile file, ImageType imageType) ;

    List<RoomImageResponse> getAllImagesByRoomNumber(String number);

    RoomImageResponse updateRoomImage(String number, Long id, MultipartFile file);

    RoomImageResponse updateImageType(String number, Long id, ImageType type);

    void deleteImageByRoomNumber(String number, Long id);
}
