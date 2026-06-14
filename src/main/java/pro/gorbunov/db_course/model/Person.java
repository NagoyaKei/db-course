package pro.gorbunov.db_course.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@NoArgsConstructor
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

    @NotBlank
    @Pattern(regexp = "^[MF]$", message = "Пол: M или F")
    private String gender;

    @Size(max = 200)
    private String contactInfo;

    public String getFullName() {
        return lastName + " " + firstName + (middleName != null && !middleName.isBlank() ? " " + middleName : "");
    }
}
