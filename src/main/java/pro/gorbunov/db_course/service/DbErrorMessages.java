package pro.gorbunov.db_course.service;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DbErrorMessages {

    public String firstLine(Throwable error) {
        String message = rootCause(error).getMessage();
        return message.lines().findFirst().orElse(message).replaceFirst("^ERROR: ", "");
    }

    private Throwable rootCause(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
