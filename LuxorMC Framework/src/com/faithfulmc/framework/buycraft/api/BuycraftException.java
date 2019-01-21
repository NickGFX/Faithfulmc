package com.faithfulmc.framework.buycraft.api;

public class BuycraftException extends Exception{
    public BuycraftException() {
    }

    public BuycraftException(String message) {
        super(message);
    }

    public BuycraftException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuycraftException(Throwable cause) {
        super(cause);
    }

    public BuycraftException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
