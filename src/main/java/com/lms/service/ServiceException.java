package com.lms.service;

/** Thrown for business-rule / validation failures (duplicate username, bad input, etc). */
public class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }
}
