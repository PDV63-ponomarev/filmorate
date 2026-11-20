package com.example.filmorate.model;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Film {

    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull
    @PastOrPresent
    private LocalDate releaseDate;

    @Positive
    private int duration;
}
