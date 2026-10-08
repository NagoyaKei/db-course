package pro.gorbunov.db_course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import pro.gorbunov.db_course.model.StoredImage;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ImageUploadReader {

    private final ImageTypeDetector imageTypeDetector;

    public StoredImage read(MultipartFile file, BindingResult errors) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            byte[] data = file.getBytes();
            Optional<String> contentType = imageTypeDetector.detect(data);
            if (contentType.isEmpty()) {
                errors.reject("image.type", "Допустимы изображения JPEG, PNG или WebP");
                return null;
            }
            return new StoredImage(contentType.get(), data);
        } catch (IOException e) {
            errors.reject("image.read", "Не удалось прочитать файл изображения");
            return null;
        }
    }
}
