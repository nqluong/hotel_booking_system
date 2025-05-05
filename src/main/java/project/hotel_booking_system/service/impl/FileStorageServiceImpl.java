package project.hotel_booking_system.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.hotel_booking_system.service.FileStorageService;

import javax.imageio.IIOException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStorageServiceImpl implements FileStorageService {

    final String UPLOAD_DIR = Paths.get(System.getProperty("user.dir"),"uploads").toString();

    @PostConstruct
    public void init(){
        try {
            Path pathFile = Paths.get(UPLOAD_DIR);
            if (!Files.exists(pathFile)){
                Files.createDirectories(pathFile);
            }
        }catch (IOException e){
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String folderPath) {
        try {
            Path uploadPath = (folderPath == null || folderPath.isEmpty())
                ? Paths.get(UPLOAD_DIR) : Paths.get(UPLOAD_DIR, folderPath);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID()+"_"+file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return Paths.get(folderPath, fileName).toString().replace("\\","/");
        }catch (IOException e){
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isBlank()){
                log.warn("File path is empty or null");
                return;
            }

            Path path = Paths.get(filePath);
            if (Files.exists(path)){
                Files.deleteIfExists(path);
            }

        }catch (IOException e){
            log.error("Failed to delete file: "+ filePath, e);
        }
    }
}
