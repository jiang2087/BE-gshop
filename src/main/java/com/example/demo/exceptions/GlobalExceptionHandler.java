package com.example.demo.exceptions;

import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serializable;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<?> handleOptimisticLock(ObjectOptimisticLockingFailureException ex){
        String entityName = ex.getPersistentClassName();
        Serializable id = (Serializable) ex.getIdentifier();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Optimistic lock conflict on " + entityName + " id=" + id);

    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handlerBadRequest(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}
