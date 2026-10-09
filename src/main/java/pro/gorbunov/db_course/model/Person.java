package pro.gorbunov.db_course.model;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Person {
    private Long personId;

    @NotBlank(message = "Имя обязательно")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String middleName;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthDate;

    @NotNull
    private Gender gender;

    @Size(max = 200)
    private String contactInfo;

    private boolean hasPhoto;

    public String getFullName() {
        return lastName + " " + firstName + (middleName != null && !middleName.isBlank() ? " " + middleName : "");
    }
}
