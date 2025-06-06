package project.hotel_booking_system.service.common;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String saveFile(MultipartFile file, String folderPath);

    void deleteFile(String filePath);
}
