package com.server.auth.oauth.util.exception;

public class JwtException extends RuntimeException{

    public JwtException(String message) {
        super(message);
    }
}
