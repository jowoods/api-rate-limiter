package com.woods.agoda.exceptions;

public class RefillInProgressException extends RuntimeException {
        public RefillInProgressException()  {
        }

        public RefillInProgressException(String message) {
            super(message);
        }

        public RefillInProgressException(Throwable cause) {
            super(cause);
        }

        public RefillInProgressException(String message, Throwable cause) {
            super(message, cause);
        }

        public RefillInProgressException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)  {
            super(message, cause, enableSuppression, writableStackTrace);
        }
}