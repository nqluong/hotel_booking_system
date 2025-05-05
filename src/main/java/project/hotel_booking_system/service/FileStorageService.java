package project.hotel_booking_system.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String saveFile(MultipartFile file, String folderPath);

    void deleteFile(String filePath);
}
