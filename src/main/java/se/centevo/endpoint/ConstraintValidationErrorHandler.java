package se.centevo.endpoint;

import java.util.Map;

import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
@RestControllerAdvice
public class ConstraintValidationErrorHandler {
    
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handle(RepositoryConstraintViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(ex.getMessage());
        problemDetail.setInstance(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri());
        problemDetail.setDetail("Object can not be saved before constraint violations have been resolved.");
        // final List<String> errors = new ArrayList<>();
        var errors = ex.getErrors().getFieldErrors().stream().map(e -> String.valueOf(e.getField()) + " : " + e.getDefaultMessage()).toList();
        problemDetail.setProperties(Map.of("errors", errors));

        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }
}