package pro.gorbunov.db_course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.gorbunov.db_course.model.StoredImage;
import pro.gorbunov.db_course.repository.PersonImageRepository;

@Service
@RequiredArgsConstructor
public class PersonPhotoService {

    private final PersonImageRepository personImageRepository;

    public void apply(Long personId, StoredImage photo, boolean removePhoto) {
        if (photo != null) {
            personImageRepository.save(personId, photo);
        } else if (removePhoto) {
            personImageRepository.delete(personId);
        }
    }
}
