package com.example.filmorate.controller;

import com.example.filmorate.exception.FilmAlreadyExistException;
import com.example.filmorate.exception.FilmNotFoundException;
import com.example.filmorate.exception.UserNotFoundException;
import com.example.filmorate.model.Film;


import com.example.filmorate.storage.FilmStorage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmStorage filmStorage;

    @GetMapping
    public List<Film> findAll(){
        return filmStorage.findAll();
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film){
        return filmStorage.create(film);
    }

    @PutMapping()
    public Film update(@RequestBody @Valid Film film){
        return filmStorage.update(film);
    }

    @GetMapping
    public Optional<Film> findById(@RequestBody @Valid long id){
        return filmStorage.findById(id);
    }

    @DeleteMapping
}
