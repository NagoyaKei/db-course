package pro.gorbunov.db_course.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.StoredImage;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PersonImageRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<StoredImage> rowMapper = (rs, rowNum) ->
            new StoredImage(rs.getString("content_type"), rs.getBytes("data"));

    public Optional<StoredImage> find(Long personId) {
        return jdbc.query("SELECT content_type, data FROM person_image WHERE person_id = ?", rowMapper, personId)
                .stream().findFirst();
    }

    public boolean exists(Long personId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM person_image WHERE person_id = ?)", Boolean.class, personId));
    }

    public void save(Long personId, StoredImage image) {
        jdbc.update(
                """
                INSERT INTO person_image (person_id, content_type, data) VALUES (?, ?, ?)
                ON CONFLICT (person_id) DO UPDATE SET content_type = EXCLUDED.content_type, data = EXCLUDED.data
                """,
                personId, image.contentType(), image.data()
        );
    }

    public void delete(Long personId) {
        jdbc.update("DELETE FROM person_image WHERE person_id = ?", personId);
    }
}
