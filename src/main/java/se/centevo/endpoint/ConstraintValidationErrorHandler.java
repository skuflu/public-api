package se.centevo.endpoint;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.microsoft.sqlserver.jdbc.SQLServerException;

import io.micrometer.tracing.Tracer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RestControllerAdvice
@Slf4j
@AllArgsConstructor
class ConstraintValidationErrorHandler {
    private final String constraintValidationErrorMessage = "Object can not be saved before constraint violations have been resolved.";
    private final String invalidArgumentErrorMessage = "Object can not be saved before invalid arguments have been resolved.";
    private final Tracer tracer;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handle(RepositoryConstraintViolationException ex) {
        log.atError().setCause(ex).log();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(ex.getMessage());
        problemDetail.setInstance(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri());
        problemDetail.setDetail(constraintValidationErrorMessage);
        
        var errors = ex.getErrors().getFieldErrors().stream().map(e -> String.valueOf(e.getField()) + " : " + e.getDefaultMessage()).toList();
        problemDetail.setProperties(
            Map.of("errors", errors, "trace-id", 
                tracer.currentTraceContext().context().traceId(),
                "timestamp", LocalDateTime.now()
            )
        );
        

        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    class SqlServerErrorNumber {
        final static int UNIQUE_KEY = 2627;
        final static int USER_ERROR = 50000;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handle(SQLServerException ex) {
        log.atError().setCause(ex).log();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        String errorMessage = switch(ex.getSQLServerError().getErrorNumber()) {
            case SqlServerErrorNumber.UNIQUE_KEY ->  "Violation of unique constraint."; 
            case SqlServerErrorNumber.USER_ERROR -> ex.getMessage();
            default -> "" ;
        };

        problemDetail.setTitle(errorMessage);
        problemDetail.setInstance(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri());
        problemDetail.setDetail(constraintValidationErrorMessage);

        problemDetail.setProperties(
            Map.of("trace-id", tracer.currentTraceContext().context().traceId(),
            "timestamp", LocalDateTime.now()
            )
        );

        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handle(IllegalArgumentException ex) {
        log.atError().setCause(ex).log();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle(ex.getMessage());
        problemDetail.setInstance(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri());
        problemDetail.setDetail(invalidArgumentErrorMessage);

        problemDetail.setProperties(
            Map.of("trace-id", tracer.currentTraceContext().context().traceId(),
            "timestamp", LocalDateTime.now()
            )
        );

        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ProblemDetail> handle(Exception ex) {
        log.atError().setCause(ex).log();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problemDetail.setTitle("Internal server error");
        problemDetail.setInstance(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri());

        problemDetail.setProperties(
            Map.of("trace-id", tracer.currentTraceContext().context().traceId(),
            "timestamp", LocalDateTime.now()
            )
        );

        return new ResponseEntity<>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}