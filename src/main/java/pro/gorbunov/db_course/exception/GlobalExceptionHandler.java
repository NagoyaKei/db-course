package pro.gorbunov.db_course.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleNotFound(RuntimeException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
