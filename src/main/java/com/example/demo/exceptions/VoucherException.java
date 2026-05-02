package com.example.demo.exceptions;

import com.example.demo.Enums.VoucherErrorCode;

public class VoucherException extends RuntimeException {

    private final VoucherErrorCode errorCode;

    public VoucherException(VoucherErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public VoucherErrorCode getErrorCode() {
        return errorCode;
    }
}