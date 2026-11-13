package com.example.filmorate.controller;

import com.example.filmorate.exception.UserAlreadyExistException;
import com.example.filmorate.exception.UserNotFoundException;
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

    @PutMapping("/{id}")
    public User update(@PathVariable int id, @RequestBody @Valid User user){

        // 1. Ищем существующего пользователя ПО ID из пути
        User existing = users.get(id);
        if (existing == null) {
            throw new UserNotFoundException("Пользователь не найден: id=" + id);
        }

        // 2. Уникальность email
        boolean emailTaken = users.values().stream()
                .filter(u -> u.getId() != id)   // исключаем самого себя
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailTaken) {
            throw new UserAlreadyExistException("Email уже занят: " + user.getEmail());
        }

        // 3. Уникальность login
        boolean loginTaken = users.values().stream()
                .filter(u -> u.getId() != id)  // исключаем самого себя
                .anyMatch(u -> u.getLogin().equals(user.getLogin()));
        if (loginTaken) {
            throw new UserAlreadyExistException("Логин уже занят: " + user.getLogin());
        }

        // 4. name опционален — подставляем login, если пусто (та же логика, что в create)
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        // 5. id неизменяем — берём из пути, игнорируя то, что пришло в теле
        user.setId(id);

        // 6. Сохраняем (put по существующему ключу перезаписывает)
        users.put(id, user);

        log.info("Обновлён пользователь: id={}, login={}", id, user.getLogin());
        return user;
    }
}
