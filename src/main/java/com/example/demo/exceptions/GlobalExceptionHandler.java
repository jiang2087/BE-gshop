package com.example.demo.exceptions;

import com.example.demo.Enums.VoucherErrorCode;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(VoucherException.class)
    public ResponseEntity<?> handleVoucherException(VoucherException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("code", ex.getErrorCode());
        response.put("message", getMessage(ex.getErrorCode()));

        return ResponseEntity.badRequest().body(response);
    }

    private String getMessage(VoucherErrorCode code) {
        return switch (code) {
            case NOT_FOUND -> "Voucher not found";
            case INACTIVE -> "Voucher is inactive";
            case EXPIRED -> "Voucher has expired";
            case OUT_OF_STOCK -> "Voucher is no longer available";
            case MIN_NOT_MET -> "Minimum order value not met";
            case NOT_OWNED -> "You do not own this voucher";
            case ALREADY_USED -> "Voucher has already been used";
            case INVALID_TYPE -> "Invalid voucher";
        };
    }
}
