package com.example.filmorate.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class User {

    @JsonProperty(access = Access.READ_ONLY)
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

