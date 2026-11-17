package com.example.filmorate;

import com.example.filmorate.controller.UserController;
import com.example.filmorate.exception.UserAlreadyExistException;
import com.example.filmorate.exception.UserNotFoundException;
import com.example.filmorate.model.User;
import jakarta.servlet.ServletException;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    private JsonNode createdUser(User user) throws Exception{
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(body);
        return  created;
    }

    private void assertFieldValidationError(User user, String message, String fieldName, String messageException) throws Exception{
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String errorMessage = message;

                    Exception ex = result.getResolvedException();
                    assertInstanceOf(MethodArgumentNotValidException.class, ex, errorMessage);

                    MethodArgumentNotValidException manve = (MethodArgumentNotValidException) ex;
                    var fieldError = manve.getBindingResult().getFieldError(fieldName);

                    assertNotNull(fieldError, errorMessage);
                    if (!messageException.isEmpty()) {
                        assertTrue(fieldError.getDefaultMessage()
                                .contains(messageException), errorMessage);
                    }
                });
    }

    private void assertUserAlreadyExist(User user, String expectedTextPart, String failMessage) {
        ServletException servletException = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))),
                failMessage
        );

        Throwable cause = servletException.getCause();
        assertInstanceOf(UserAlreadyExistException.class, cause, failMessage);
        assertTrue(cause.getMessage().contains(expectedTextPart),
                "Сообщение исключения должно содержать: " + expectedTextPart);
    }

    @Test
    void createCorrectUserAndSave() throws Exception{
        User user = User.builder()
                .email("test@mail.ru")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.parse("2007-12-03"))
                .build();


        JsonNode created = createdUser(user);

        assertNotNull(created, "Контроллер должен вернуть созданного пользователя");
        assertEquals(1, created.get("id").asInt(),
                "Первому пользователю должен быть присвоен id = 1");
        assertEquals("test@mail.ru", created.get("email").asText(),
                "Email должен совпадать с переданным в запросе");
        assertEquals("testlogin", created.get("login").asText(),
                "Login должен совпадать с переданным в запросе");
        assertEquals("Test User", created.get("name").asText(),
                "Name должен совпадать с переданным в запросе");
        assertEquals("2007-12-03", created.get("birthday").asText(),
                "Birthday должен совпадать с переданным в запросе");
    }

    @Test
    void createUserWhenNameIsBlank() throws Exception{
        User user = User.builder()
                .email("noname@mail.ru")
                .login("nonamelogin")
                .name("")
                .birthday(LocalDate.parse("2007-12-03"))
                .build();

        JsonNode created = createdUser(user);

        assertEquals(created.get("login").asText(), created.get("name").asText(),
                "Если name пустой, должно подставляться значение login");
    }

    @Test
    void createUserWithDuplicateEmailShouldThrowException() throws Exception{

        User user1 = User.builder()
                .email("duplicate@mail.ru")
                .login("login")
                .build();

        User user2 = User.builder()
                .email("duplicate@mail.ru")
                .login("login2")
                .build();

        createdUser(user1);

        assertUserAlreadyExist(
                user2,
                "duplicate@mail.ru",
                "При дубликате email должно выбрасываться UserAlreadyExistException"
        );
    }

    @Test
    void createUserWithDuplicateLoginShouldThrowException() throws Exception{
        User user1 = User.builder()
                .email("some@mail.ru")
                .login("dupLogin")
                .build();

        User user2 = User.builder()
                .email("some2@mail.ru")
                .login("dupLogin")
                .build();

        createdUser(user1);

        assertUserAlreadyExist(
                user2,
                "dupLogin",
                "При дубликате login должно выбрасываться UserAlreadyExistException"
        );
    }

    @Test
    void createTwoUsersShouldGetDifferentIds() throws Exception{
        User user1 = User.builder()
                .email("some@mail.ru")
                .login("login1")
                .build();

        User user2 = User.builder()
                .email("some2@mail.ru")
                .login("login2")
                .build();

        JsonNode created1 = createdUser(user1);
        JsonNode created2 = createdUser(user2);

        assertNotEquals(created1.get("id").asInt(), created2.get("id").asInt(),
                "У разных пользователей должны быть разные id");
    }

    @Test
    void findAllShouldReturnAllCreatedUsers() throws Exception{
        User user1 = User.builder()
                .email("some@mail.ru")
                .login("login1")
                .build();

        User user2 = User.builder()
                .email("some2@mail.ru")
                .login("login2")
                .build();

        createdUser(user1);
        createdUser(user2);

        MvcResult result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode allUsers = objectMapper.readTree(result.getResponse().getContentAsString());

        assertTrue(allUsers.isArray(), "Ответ должен быть JSON-массивом");
        assertEquals(2, allUsers.size(), "Должно вернуться 2 пользователя");
        assertEquals("login1", allUsers.get(0).get("login").asText(), "Логин первого пользователя должен совпадать");
        assertEquals("login2", allUsers.get(1).get("login").asText(), "Логин второго пользователя должен совпадать");
    }


    @Test
    void createUserWithWrongEmailShouldThrowException() throws Exception{
        User user = User.builder()
                .email("wrongmail.ru")
                .login("login")
                .build();

        assertFieldValidationError(user,
                "Должно возникнуть сообщение о неверном формате email",
                "email",
                "Email должен содержать @ и домен с точкой");

    }

    @Test
    void createUserWithEmptyLoginShouldThrowException() throws Exception{
        User user = User.builder()
                .email("mail@mail.ru")
                .login("")
                .build();

        assertFieldValidationError(user,
                "Должно возникнуть сообщение о пустом логине",
                "login",
                "");
    }

    @Test
    void createUserWithEmptyEmailShouldThrowException() throws Exception{
        User user = User.builder()
                .email("")
                .login("login")
                .build();

        assertFieldValidationError(user,
                "Должно возникнуть сообщение о пустой почте",
                "email",
                "");
    }

    @Test
    void createUserWithFutureBirthdayShouldThrowException() throws Exception{
        User user = User.builder()
                .email("mail@mail.ru")
                .login("login")
                .birthday(LocalDate.now().plus(5, ChronoUnit.DAYS))
                .build();

        assertFieldValidationError(user,
                "Должно возникнуть сообщение о неверной дате рождения",
                "birthday",
                "");
    }

    @Test
    void createUserWithLoginWithWhitespaceShouldThrowException() throws Exception{
        User user = User.builder()
                .email("mail@mail.ru")
                .login("login whitespace")
                .birthday(LocalDate.now().plus(5, ChronoUnit.DAYS))
                .build();

        assertFieldValidationError(user,
                "Должно возникнуть сообщение об ошибке логина",
                "login",
                "login не должен содержать пробелов");
    }

    @Test
    void putUser() throws Exception {
        User user1 = User.builder()
                .email("test@mail.ru")
                .login("login")
                .name("User")
                .birthday(LocalDate.parse("2007-12-03"))
                .build();

        // 1. Создаём пользователя и достаём присвоенный ему id из ответа сервера
        JsonNode created = createdUser(user1);
        int createdId = created.get("id").asInt();

        // 2. Устанавливаем этот id и новые данные в объект перед PUT-запросом
        user1.setId(createdId);
        user1.setEmail("test2@mail.ru");
        user1.setLogin("login2");
        user1.setName("User2");
        user1.setBirthday(LocalDate.parse("2017-12-03"));

        // 3. Отправляем PUT-запрос и сохраняем новый ответ в переменную updated
        JsonNode updated = updatedUser(user1);

        // 4. Проверяем именно updated (а не старый created)
        assertEquals(createdId, updated.get("id").asInt(),
                "У пользователя должен был сохраниться ID = 1");
        assertEquals("test2@mail.ru", updated.get("email").asText(),
                "Email должен измениться");
        assertEquals("login2", updated.get("login").asText(),
                "Login должен измениться");
        assertEquals("User2", updated.get("name").asText(),
                "Name должен измениться");
        assertEquals("2017-12-03", updated.get("birthday").asText(),
                "Birthday должен измениться");
    }

//    @Test
//    void putUser() throws Exception{
//        User user1 = User.builder()
//                .email("test@mail.ru")
//                .login("login")
//                .name("User")
//                .birthday(LocalDate.parse("2007-12-03"))
//                .build();
//
//        JsonNode created = createdUser(user1);
//        int userId = created.get("id").asInt();
//
//        User userToUpdate = User.builder()
//                .id(userId)
//                .email("test2@mail.ru")
//                .login("login2")
//                .name("User2")
//                .birthday(LocalDate.parse("2017-12-03"))
//                .build();
//
//        MvcResult result = mockMvc.perform(put("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userToUpdate)))
//                .andExpect(status().isOk())
//                .andReturn();
//        JsonNode updated = objectMapper.readTree(result.getResponse().getContentAsString());
//
//
//        assertEquals(userId, updated.get("id").asInt(),
//                "У пользователя должен сохраниться прежний ID");
//        assertEquals("test2@mail.ru", updated.get("email").asText(),
//                "Email должен измениться");
//        assertEquals("login2", updated.get("login").asText(),
//                "Login должен измениться");
//        assertEquals("User2", updated.get("name").asText(),
//                "Name должен измениться");
//        assertEquals("2017-12-03", updated.get("birthday").asText(),
//                "Birthday должен измениться");
//    }


/*

@Test
void putUserWithUnknownIdShouldThrowException() {
    User unknownUser = User.builder()
            .id(999)
            .email("unknown@mail.ru")
            .login("unknown")
            .name("Unknown")
            .birthday(LocalDate.parse("2000-01-01"))
            .build();

    ServletException servletException = assertThrows(
            ServletException.class,
            () -> mockMvc.perform(put("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(unknownUser))),
            "При попытке обновить несуществующего пользователя должно выбрасываться исключение"
    );

    // Подставьте здесь ваш класс исключения (например, NotFoundException.class)
    assertInstanceOf(NotFoundException.class, servletException.getCause(),
            "Причиной должно быть NotFoundException");
}

 */

}

