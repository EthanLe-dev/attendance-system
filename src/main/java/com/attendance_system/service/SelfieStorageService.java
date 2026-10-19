package com.attendance_system.service;

import com.attendance_system.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SelfieStorageService {

    private final UploadProperties uploadProperties;

    public String store(MultipartFile selfie) {

        if (selfie == null || selfie.isEmpty()) {
            throw new IllegalArgumentException(
                    "Selfie file is empty"
            );
        }

        if (selfie.getSize() > uploadProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("Ảnh selfie vượt quá dung lượng cho phép.");
        }

        ImageType imageType = inspectImage(selfie);
        String filename = UUID.randomUUID() + imageType.extension();

        Path uploadDirectory = Paths.get(uploadProperties.getSelfieDirectory()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadDirectory);

            Path targetPath =
                    uploadDirectory.resolve(filename).normalize();

            if (!targetPath.startsWith(uploadDirectory.toAbsolutePath().normalize())) {
                throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ.");
            }

            selfie.transferTo(targetPath);

            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to store selfie",
                    e
            );
        }
    }

    private ImageType inspectImage(MultipartFile selfie) {
        try (InputStream input = selfie.getInputStream();
             ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw new IllegalArgumentException("Selfie phải là ảnh JPEG hoặc PNG hợp lệ.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Selfie phải là ảnh JPEG hoặc PNG hợp lệ.");
            }

            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase();
                ImageType imageType = switch (format) {
                    case "jpeg", "jpg" -> new ImageType(".jpg");
                    case "png" -> new ImageType(".png");
                    default -> throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPEG hoặc PNG.");
                };

                reader.setInput(imageInput, true, true);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels <= 0 || pixels > uploadProperties.getMaxImagePixels()) {
                    throw new IllegalArgumentException("Kích thước ảnh selfie không hợp lệ.");
                }
                return imageType;
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể đọc ảnh selfie hợp lệ.", e);
        }
    }

    public void delete(String selfiePath) {

        if (selfiePath == null || selfiePath.isBlank()) {
            return;
        }

        try {
            Path path = safePath(selfiePath);

            Files.deleteIfExists(path);

        } catch (IOException e) {
            System.err.println("Failed to delete selfie: " + selfiePath);
        }
    }

    public Path load(String selfiePath) {
        Path path = safePath(selfiePath);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Không tìm thấy ảnh selfie.");
        }
        return path;
    }

    private Path safePath(String selfiePath) {
        Path uploadDirectory = Paths.get(uploadProperties.getSelfieDirectory()).toAbsolutePath().normalize();
        Path path = Paths.get(selfiePath).toAbsolutePath().normalize();
        if (!path.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ.");
        }
        return path;
    }

    private record ImageType(String extension) {
    }
}
