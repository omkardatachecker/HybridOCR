package com.datachecker.hybridocrlib.exception;

import androidx.annotation.Nullable;

public class ScanFailedException extends Exception{
    private String message;

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ScanFailedException(String message){
        super();
        this.message = message;
    }
}
