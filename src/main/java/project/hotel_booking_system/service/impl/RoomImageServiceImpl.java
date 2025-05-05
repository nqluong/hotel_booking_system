package project.hotel_booking_system.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.hotel_booking_system.dto.response.RoomImageResponse;
import project.hotel_booking_system.dto.response.RoomResponse;
import project.hotel_booking_system.enums.ImageType;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.RoomImageMapper;
import project.hotel_booking_system.model.Room;
import project.hotel_booking_system.model.RoomImage;
import project.hotel_booking_system.repository.RoomImageRepository;
import project.hotel_booking_system.repository.RoomRepository;
import project.hotel_booking_system.service.FileStorageService;
import project.hotel_booking_system.service.RoomImageService;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomImageServiceImpl implements RoomImageService {

    RoomRepository roomRepository;
    RoomImageRepository roomImageRepository;
    RoomImageMapper roomImageMapper;
    FileStorageService fileStorageService;

    @Override
    public RoomImageResponse uploadRoomImage(String roomNumber, MultipartFile file, ImageType imageType) {

            Room room = roomRepository.findByRoomNumber(roomNumber).orElseThrow(
                    () ->  new AppException(ErrorCode.ROOM_NOT_FOUND)
            );

            String imagePath = fileStorageService.saveFile(file, "room_images");

            RoomImage roomImage = RoomImage.builder()
                    .room(room)
                    .imageUrl(imagePath)
                    .imageType(imageType)
                    .createdAt(LocalDateTime.now())
                    .build();

            roomImageRepository.save(roomImage);
            return roomImageMapper.toImageResponse(roomImage);

    }

    @Override
    public List<RoomImageResponse> getAllImagesByRoomNumber(String number) {
        Room room = roomRepository.findByRoomNumber(number).orElseThrow(
                () ->  new AppException(ErrorCode.ROOM_NOT_FOUND)
        );
        return roomImageRepository.findByRoom_Id(room.getId())
                .stream()
                .map(roomImageMapper::toImageResponse)
                .toList();

    }

    @Override
    public RoomImageResponse updateRoomImage(String number, Long id, MultipartFile file) {
        Room room = roomRepository.findByRoomNumber(number).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND)
        );
        RoomImage image = roomImageRepository.findByIdAndRoom(id, room)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));

        fileStorageService.deleteFile(image.getImageUrl());

        String newImagePath = fileStorageService.saveFile(file, "room_images");
        image.setImageUrl(newImagePath);
        image.setCreatedAt(LocalDateTime.now());

        return roomImageMapper.toImageResponse(roomImageRepository.save(image));
    }

    @Override
    public RoomImageResponse updateImageType(String number, Long id, ImageType type) {
        Room room = roomRepository.findByRoomNumber(number).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND)
        );
        RoomImage image = roomImageRepository.findByIdAndRoom(id, room)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        image.setImageType(type);

        return roomImageMapper.toImageResponse(roomImageRepository.save(image));
    }

    @Override
    public void deleteImageByRoomNumber(String number, Long id) {
        Room room = roomRepository.findByRoomNumber(number).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND)
        );

        RoomImage image = roomImageRepository.findByIdAndRoom(id, room)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));

        fileStorageService.deleteFile(image.getImageUrl());
        roomImageRepository.delete(image);

    }
}
