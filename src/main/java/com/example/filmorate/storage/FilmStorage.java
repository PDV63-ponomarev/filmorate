package com.example.filmorate.storage;

import com.example.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    void delete(Long id);

    Optional<Film> findById(Long id);

}
