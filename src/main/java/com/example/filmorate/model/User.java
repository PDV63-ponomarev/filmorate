package com.example.filmorate.model;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    private Integer id;

    @Email(regexp = ".+@.+\\..+", message = "Email должен содержать @ и домен с точкой")
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "\\S+", message = "login не должен содержать пробелов")
    private String login;

    private String name;

    @Past
    private LocalDate birthday;
}

