package project.hotel_booking_system.service;

import org.springframework.web.multipart.MultipartFile;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.enums.ImageType;

import java.io.IOException;
import java.util.List;

public interface RoomImageService {

    RoomImageResponse uploadRoomImage(Long roomId, MultipartFile file, ImageType imageType) ;

    List<RoomImageResponse> getAllImagesByRoomNumber(Long roomId);

    RoomImageResponse updateRoomImage(Long roomId, Long id, MultipartFile file);

    RoomImageResponse updateImageType(Long roomId, Long id, ImageType type);

    void deleteImageByRoomNumber(Long roomId, Long id);
}
