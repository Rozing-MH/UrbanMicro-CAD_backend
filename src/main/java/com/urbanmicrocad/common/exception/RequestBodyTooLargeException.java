package com.urbanmicrocad.common.exception;

import java.io.IOException;

public class RequestBodyTooLargeException extends IOException {
    public RequestBodyTooLargeException() {
        super("request body too large");
    }
}
