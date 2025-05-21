package project.hotel_booking_system.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.hotel_booking_system.dto.response.RoomImageResponse;
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


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomImageServiceImpl implements RoomImageService {

    RoomRepository roomRepository;
    RoomImageRepository roomImageRepository;
    RoomImageMapper roomImageMapper;
    FileStorageService fileStorageService;

    @Override
    public RoomImageResponse uploadRoomImage(Long roomId, MultipartFile file, ImageType imageType) {

            Room room = roomRepository.findById(roomId).orElseThrow(
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
    public List<RoomImageResponse> getAllImagesByRoomNumber(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () ->  new AppException(ErrorCode.ROOM_NOT_FOUND)
        );
        return roomImageRepository.findByRoom_Id(room.getId())
                .stream()
                .map(roomImageMapper::toImageResponse)
                .toList();

    }

    @Override
    public RoomImageResponse updateRoomImage(Long number, Long id, MultipartFile file) {
        Room room = roomRepository.findById(number).orElseThrow(
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
    public RoomImageResponse updateImageType(Long number, Long id, ImageType type) {
        Room room = roomRepository.findById(number).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND)
        );
        RoomImage image = roomImageRepository.findByIdAndRoom(id, room)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        image.setImageType(type);

        return roomImageMapper.toImageResponse(roomImageRepository.save(image));
    }

    @Override
    public void deleteImageByRoomNumber(Long number, Long id) {
        Room room = roomRepository.findById(number).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND)
        );

        RoomImage image = roomImageRepository.findByIdAndRoom(id, room)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));

        fileStorageService.deleteFile(image.getImageUrl());
        roomImageRepository.delete(image);

    }
}
