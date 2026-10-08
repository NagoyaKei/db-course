package pro.gorbunov.db_course.controller;

import java.util.Optional;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import pro.gorbunov.db_course.model.StoredImage;
import pro.gorbunov.db_course.repository.PersonImageRepository;
import pro.gorbunov.db_course.service.PhotoThumbnailService;

@Controller
@RequestMapping("/persons")
public class PersonPhotoController {

    private static final String THUMBNAIL_ETAG_SUFFIX = "-thumbnail";

    private final PersonImageRepository personImageRepository;
    private final PhotoThumbnailService photoThumbnailService;

    public PersonPhotoController(PersonImageRepository personImageRepository, PhotoThumbnailService photoThumbnailService) {
        this.personImageRepository = personImageRepository;
        this.photoThumbnailService = photoThumbnailService;
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Long id, WebRequest request) {
        return respond(id, request, false);
    }

    @GetMapping("/{id}/photo/thumbnail")
    public ResponseEntity<byte[]> thumbnail(@PathVariable Long id, WebRequest request) {
        return respond(id, request, true);
    }

    private ResponseEntity<byte[]> respond(Long id, WebRequest request, boolean thumbnail) {
        Optional<StoredImage> photo = personImageRepository.find(id);
        if (photo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String contentHash = DigestUtils.md5DigestAsHex(photo.get().data());
        String etag = "\"" + contentHash + (thumbnail ? THUMBNAIL_ETAG_SUFFIX : "") + "\"";
        if (request.checkNotModified(etag)) {
            return null;
        }
        StoredImage image = thumbnail ? photoThumbnailService.thumbnail(photo.get(), contentHash) : photo.get();
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.data());
    }
}
