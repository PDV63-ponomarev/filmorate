package com.example.filmorate.controller;

import com.example.filmorate.exception.InvalidEmailException;
import com.example.filmorate.exception.UserAlreadyExistException;
import com.example.filmorate.model.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public List<User> findAll(){
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User create(@RequestBody @Valid User user){
        String email = user.getEmail();
        String login = user.getLogin();

        // Проверка на уникальность
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        boolean loginExists = users.values().stream()
                .anyMatch(u -> u.getLogin().equals(login));

        if (emailExists) throw new UserAlreadyExistException("email " + email + " уже существует");
        if (loginExists) throw new UserAlreadyExistException("login " + login + " уже существует");

        if (user.getName() == null || user.getName().isBlank()){
            user.setName(login);
        }

        user.setId(nextId++);
        log.info("Добавлен пользователь: {}", user.getName());

        // Сохранение
        users.put(user.getId(), user);
        return user;
    }

}
