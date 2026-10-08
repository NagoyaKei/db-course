package pro.gorbunov.db_course.service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pro.gorbunov.db_course.model.StoredImage;

@Service
public class PhotoThumbnailService {

    private static final int MAX_WIDTH = 96;
    private static final int MAX_HEIGHT = 120;
    private static final int CACHE_SIZE = 200;
    private static final String THUMBNAIL_FORMAT = "png";
    private static final String THUMBNAIL_CONTENT_TYPE = "image/png";

    private final Map<String, StoredImage> cache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, StoredImage> eldest) {
            return size() > CACHE_SIZE;
        }
    });

    public StoredImage thumbnail(StoredImage original, String contentHash) {
        StoredImage cached = cache.get(contentHash);
        if (cached != null) {
            return cached;
        }
        StoredImage thumbnail = resize(original).orElse(original);
        cache.put(contentHash, thumbnail);
        return thumbnail;
    }

    private Optional<StoredImage> resize(StoredImage original) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(original.data()));
            if (source == null) {
                return Optional.empty();
            }
            return Optional.of(new StoredImage(THUMBNAIL_CONTENT_TYPE, encode(scale(source))));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private BufferedImage scale(BufferedImage source) {
        double ratio = Math.min(1.0, Math.min((double) MAX_WIDTH / source.getWidth(), (double) MAX_HEIGHT / source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        int type = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage target = new BufferedImage(width, height, type);
        Graphics2D graphics = target.createGraphics();
        graphics.drawImage(source.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        graphics.dispose();
        return target;
    }

    private byte[] encode(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, THUMBNAIL_FORMAT, out);
        return out.toByteArray();
    }
}
