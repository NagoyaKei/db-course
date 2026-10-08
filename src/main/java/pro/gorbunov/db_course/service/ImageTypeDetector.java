package pro.gorbunov.db_course.service;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ImageTypeDetector {

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_SIGNATURE = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP_SIGNATURE = {'W', 'E', 'B', 'P'};
    private static final int WEBP_OFFSET = 8;

    public Optional<String> detect(byte[] data) {
        if (hasSignature(data, 0, JPEG_SIGNATURE)) {
            return Optional.of("image/jpeg");
        }
        if (hasSignature(data, 0, PNG_SIGNATURE)) {
            return Optional.of("image/png");
        }
        if (hasSignature(data, 0, RIFF_SIGNATURE) && hasSignature(data, WEBP_OFFSET, WEBP_SIGNATURE)) {
            return Optional.of("image/webp");
        }
        return Optional.empty();
    }

    private boolean hasSignature(byte[] data, int offset, byte[] signature) {
        if (data.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[offset + i] != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
