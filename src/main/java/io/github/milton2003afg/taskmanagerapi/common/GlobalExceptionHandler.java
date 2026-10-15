package io.github.milton2003afg.taskmanagerapi.common;

import io.github.milton2003afg.taskmanagerapi.exception.InvalidCredentialsException;
import io.github.milton2003afg.taskmanagerapi.exception.ResourceNotFoundException;
import io.github.milton2003afg.taskmanagerapi.exception.UserAlreadyExistsException;
import io.github.milton2003afg.taskmanagerapi.exception.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ProblemDetail builProblemDetail(HttpStatus httpStatus,String title , String detail){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                httpStatus,
                detail
        );
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleConflict(UserAlreadyExistsException ex){
        return builProblemDetail(HttpStatus.CONFLICT,"Account Conflict" , ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleArgumentNotValid(MethodArgumentNotValidException ex){
        Map<String, String> errors = ex.getBindingResult()
                        .getFieldErrors().stream()
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                                (existing, replacement) -> existing
                        ));

        ProblemDetail problemDetail = builProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Request Content", "Validation failed");
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex){
        return builProblemDetail(HttpStatus.CONFLICT,"Resource Already Exists",
                "The operation could not be completed because the resource or a unique field already exists");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInternalServerError(Exception ex){
        return builProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Internal server error");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handelInvalidCredentialsError(InvalidCredentialsException ex){
        return builProblemDetail(HttpStatus.UNAUTHORIZED,"Invalid Credentials", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserDoesNotExist(UserNotFoundException ex){
        return builProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid Authentication", "Invalid authentication");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex){
        return builProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", "The resource was not found");
    }
}
