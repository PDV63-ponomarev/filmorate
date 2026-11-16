package com.example.filmorate;

import com.example.filmorate.controller.UserController;


import com.example.filmorate.exception.UserAlreadyExistException;
import com.example.filmorate.model.User;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    private UserController userController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userController = new UserController();

        //симуляция  для http
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();
        objectMapper = JsonMapper.builder().build();
    }

    @Test
    void createCorrectUserAndSave() throws Exception{
        User user = User.builder()
                .email("test@mail.ru")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.parse("2007-12-03"))
                .build();

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        User created = objectMapper.readValue(body, User.class);

        assertNotNull(created, "Контроллер должен вернуть созданного пользователя");
        assertEquals(1, created.getId(), "Первому пользователю должен быть присвоен id = 1");
        assertEquals("test@mail.ru", created.getEmail(), "Email должен совпадать с переданным в запросе");
        assertEquals("testlogin", created.getLogin(), "Login должен совпадать с переданным в запросе");
        assertEquals("Test User", created.getName(), "Name должен совпадать с переданным в запросе");
    }

    @Test
    void createCorrectUserAndSave2() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.parse("2007-12-03"))
                .build();

        User created = userController.create(user);

        assertNotNull(created, "Контроллер должен вернуть созданного пользователя");
        assertEquals(1, created.getId(), "Первому пользователю должен быть присвоен id = 1");
        assertEquals("test@mail.ru", created.getEmail());
        assertEquals("testlogin", created.getLogin());
        assertEquals("Test User", created.getName());
    }
/*
    @Test
    void createUserWhenNameIsBlank(){
        User user = new User();
        user.setEmail("noname@mail.ru");
        user.setLogin("nonamelogin");
        user.setName(""); // пустое имя

        User created = userController.create(user);

        assertEquals("nonamelogin", created.getName(),
                "Если name пустой, должно подставиться значение login");
    }

    @Test
    void createUserWithDuplicateEmailShouldThrowException() {
        User first = new User();
        first.setEmail("dup@mail.ru");
        first.setLogin("login1");
        userController.create(first);

        User second = new User();
        second.setEmail("dup@mail.ru");
        second.setLogin("login2");

        UserAlreadyExistException exception = assertThrows(
                UserAlreadyExistException.class,
                () -> userController.create(second),
                "При дубликате email должно выбрасываться UserAlreadyExistException"
        );

        assertTrue(exception.getMessage().contains("dup@mail.ru"),
                "Сообщение должно содержать проблемный email");
    }

    @Test
    void createUserWithDuplicateLoginShouldThrowException() {
        User first = new User();
        first.setEmail("one@mail.ru");
        first.setLogin("sameLogin");
        userController.create(first);

        User second = new User();
        second.setEmail("two@mail.ru");
        second.setLogin("sameLogin");

        assertThrows(UserAlreadyExistException.class,
                () -> userController.create(second));
    }

    @Test
    void createUser_twoUsers_shouldGetDifferentIds() {
        User first = new User();
        first.setEmail("a@mail.ru");
        first.setLogin("loginA");

        User second = new User();
        second.setEmail("b@mail.ru");
        second.setLogin("loginB");

        User createdFirst = userController.create(first);
        User createdSecond = userController.create(second);

        assertEquals(1, createdFirst.getId());
        assertEquals(2, createdSecond.getId());
        assertNotEquals(createdFirst.getId(), createdSecond.getId(),
                "У разных пользователей должны быть разные id");
    }

    @Test
    void findAll_shouldReturnAllCreatedUsers() {
        User first = new User();
        first.setEmail("a@mail.ru");
        first.setLogin("loginA");

        User second = new User();
        second.setEmail("b@mail.ru");
        second.setLogin("loginB");

        userController.create(first);
        userController.create(second);

        var all = userController.findAll();

        assertEquals(2, all.size(), "Должно вернуться 2 пользователя");
    }

 */
}

