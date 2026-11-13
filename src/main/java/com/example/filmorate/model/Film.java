package com.example.filmorate.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class Film {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int id;

    @NotBlank
    private String name;

    @NotNull
    @Size(max = 200)
    private String description;

    @NotNull
    @PastOrPresent
    private Instant releaseDate;

    @PositiveOrZero
    private int duration;
}
