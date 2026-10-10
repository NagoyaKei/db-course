package pro.gorbunov.db_course.model;

import java.time.LocalDate;
import java.time.Period;
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

    public String getAgeLabel() {
        if (birthDate == null) {
            return "";
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age + " " + yearsWord(age);
    }

    private static String yearsWord(int age) {
        int lastTwo = age % 100;
        int last = age % 10;
        if (lastTwo >= 11 && lastTwo <= 14) {
            return "лет";
        }
        if (last == 1) {
            return "год";
        }
        return last >= 2 && last <= 4 ? "года" : "лет";
    }

    public String getFullName() {
        return lastName + " " + firstName + (middleName != null && !middleName.isBlank() ? " " + middleName : "");
    }
}
