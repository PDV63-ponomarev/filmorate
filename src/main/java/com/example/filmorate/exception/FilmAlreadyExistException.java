package com.example.filmorate.exception;

public class FilmAlreadyExistException extends RuntimeException {
    public FilmAlreadyExistException(String message) {
        super(message);
    }
}
