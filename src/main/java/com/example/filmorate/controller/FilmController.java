package com.example.filmorate.controller;

import com.example.filmorate.exception.FilmAlreadyExistException;
import com.example.filmorate.exception.FilmNotFoundException;
import com.example.filmorate.exception.UserNotFoundException;
import com.example.filmorate.model.Film;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public List<Film> findAll(){
        log.info("GET. Запрос списка фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film){
        log.info("POST. Создание записи нового фильма");
        String name = film.getName();

        // Проверка на уникальность
        boolean nameExist = films.values().stream()
                .anyMatch(u -> u.getName().equalsIgnoreCase(name));

        if (nameExist) throw new FilmAlreadyExistException("Фильм с таким названием уже существует: " + name);

        film.setId(nextId++);
        log.info("Добавлен фильм: {}", film.getName());

        // Сохранение
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping()
    public Film update( @RequestBody @Valid Film film){

        // 1. Проверяем, что id передан и такой фильм существует
        if (film.getId() == null) { // если id в модели типа Integer (рекомендуется)
            throw new UserNotFoundException("Id должен быть указан");
        }
        int id = film.getId();

        log.info("PUT. Редактирование фильма с ид: {}", id);

        Film existing = films.get(id);
        if (existing == null) {
            throw new FilmNotFoundException("Фильм не найден: id=" + id);
        }

        // 2. Уникальность имени
        boolean nameExist = films.values().stream()
                .filter(u -> u.getId() != id)   // исключаем самого себя
                .anyMatch(u -> u.getName().equalsIgnoreCase(film.getName()));
        if (nameExist) {
            throw new FilmAlreadyExistException("Название фильма уже занято: " + film.getName());
        }

        // 3. id неизменяем — берём из пути, игнорируя то, что пришло в теле
        film.setId(id);

        // 4. Сохраняем (put по существующему ключу перезаписывает)
        films.put(id, film);

        log.info("Обновлён фильм: id={}, login={}", id, film.getName());
        return film;
    }
}
