package com.example.filmorate.model;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class Film {
    private int id;

    @NotBlank
    private String name;

    private String description;
    private Instant releaseDate;

    @PositiveOrZero
    private int duration;
}
