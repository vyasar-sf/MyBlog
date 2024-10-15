package com.practical.myblog.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j // Simple Logging Facade for Java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostValidationException.class)
    public ResponseEntity<String> handlePostValidationException(PostValidationException exception) {
        log.error("Post entity error occurred: ", exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TagValidationException.class)
    public ResponseEntity<String> handleTagValidationException(TagValidationException exception) {
        log.error("Tag entity error occurred: ", exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<String> handleUserValidationException(UserValidationException exception) {
        log.error("User entity error occurred: ", exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
