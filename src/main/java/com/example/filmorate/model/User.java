package com.example.filmorate.model;

import java.time.Instant;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
public class User {

    private int id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "\\S+", message = "login не должен содержать пробелов")
    private String login;

    private String name;

    @Past
    private Instant birthday;
}

