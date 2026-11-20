package com.example.filmorate;

import com.example.filmorate.controller.FilmController;
import com.example.filmorate.exception.FilmAlreadyExistException;
import com.example.filmorate.model.Film;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FilmControllerTest {
    private FilmController filmController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();

        //симуляция  http для тестов
        mockMvc = MockMvcBuilders.standaloneSetup(filmController)
                .build();
        objectMapper = JsonMapper.builder().build();
    }

    private JsonNode createdFilm(Film film) throws Exception{
        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(body);
        return  created;
    }

    private void assertFieldValidationError(Film film, String message, String fieldName) throws Exception{
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String errorMessage = message;

                    Exception ex = result.getResolvedException();
                    assertInstanceOf(MethodArgumentNotValidException.class, ex, errorMessage);

                    MethodArgumentNotValidException manve = (MethodArgumentNotValidException) ex;
                    var fieldError = manve.getBindingResult().getFieldError(fieldName);

                    assertNotNull(fieldError, errorMessage);
                });
    }

    @Test
    void findAllShouldReturnAllCreatedUsers() throws Exception{
        Film film1 = Film.builder()
                .name("film1")
                .description("description")
                .releaseDate(LocalDate.parse("2001-01-01"))
                .duration(120)
                .build();

        Film film2 = Film.builder()
                .name("film2")
                .description("description2")
                .releaseDate(LocalDate.parse("2002-02-02"))
                .duration(60)
                .build();

        createdFilm(film1);
        createdFilm(film2);

        MvcResult result = mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode allFilms = objectMapper.readTree(result.getResponse().getContentAsString());

        assertTrue(allFilms.isArray(), "Ответ должен быть JSON-массивом");
        assertEquals(2, allFilms.size(), "Должно вернуться 2 фильма");
        assertEquals("film1", allFilms.get(0).get("name").asText(),
                "Название первого фильма должен совпадать");
        assertEquals("film2", allFilms.get(1).get("name").asText(),
                "Название второго фильма должен совпадать");
    }


    @Test
    void createCorrectFilm() throws Exception{
        Film film = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.parse("2001-01-01"))
                .duration(120)
                .build();

        JsonNode created = createdFilm(film);

        assertNotNull(created, "Контроллер должен вернуть созданный фильм");
        assertEquals(1, created.get("id").asInt(),
                "Первому пользователю должен быть присвоен id = 1");
        assertEquals("film", created.get("name").asText(),
                "Name должен совпадать с переданным в запросе");
        assertEquals("description", created.get("description").asText(),
                "Description должен совпадать с переданным в запросе");
        assertEquals("2001-01-01", created.get("releaseDate").asText(),
                "ReleaseDate должен совпадать с переданным в запросе");
        assertEquals("120", created.get("duration").asText(),
                "Duration должен совпадать с переданным в запросе");
    }


    @Test
    void createFilmWithDuplicateNameShouldThrowException() throws Exception{

        Film film1 = Film.builder()
                .name("duplicateName")
                .description("description")
                .releaseDate(LocalDate.parse("2001-01-01"))
                .duration(120)
                .build();

        Film film2 = Film.builder()
                .name("duplicateName")
                .description("description2")
                .releaseDate(LocalDate.parse("2002-01-01"))
                .duration(122)
                .build();

        createdFilm(film1);

        ServletException servletException = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2))),
                "При дубликате фильма должно выбрасываться FilmAlreadyExistException"
        );

        Throwable cause = servletException.getCause();
        assertInstanceOf(FilmAlreadyExistException.class, cause, "FilmAlreadyExistException");
        assertTrue(cause.getMessage().contains("duplicateName"),
                "Сообщение исключения должно содержать: " + "duplicateName");
    }

    @Test
    void createFilmWithEmptyNameShouldThrowException() throws Exception{
        Film film = Film.builder()
                .name("")
                .description("description")
                .releaseDate(LocalDate.parse("2001-01-01"))
                .duration(120)
                .build();

        assertFieldValidationError(film,
                "Должна появиться ошибка о пустом name",
                "name");
    }

    @Test
    void createFilmWithEmptyDescriptionShouldThrowException() throws Exception{
        Film film = Film.builder()
                .name("empty description")
                .description("")
                .releaseDate(LocalDate.parse("2001-01-01"))
                .duration(120)
                .build();

        assertFieldValidationError(film,
                "Должна появиться ошибка о пустом description",
                "description");
    }

    @Test
    void createFilmWithBigDescriptionShouldThrowException() throws Exception{
        Film film = Film.builder()
                .name("empty description")
                .description("a".repeat(201))
                .releaseDate(LocalDate.parse("2001-01-01"))
                .duration(120)
                .build();

        assertFieldValidationError(film,
                "Должна появиться ошибка о слишком большом описании",
                "description");
    }

    @Test
    void createFilmWithEmptyReleaseDateShouldThrowException() throws Exception{
        Film film = Film.builder()
                .name("empty date")
                .description("description")
                .duration(120)
                .build();

        assertFieldValidationError(film,
                "Должна появиться ошибка о пустой дате релиза",
                "releaseDate");
    }

    @Test
    void createFilmWithFutureReleaseDateShouldThrowException() throws Exception{
        Film film = Film.builder()
                .name("empty date")
                .description("description")
                .releaseDate(LocalDate.now().plus(1, ChronoUnit.DAYS))
                .duration(120)
                .build();

        assertFieldValidationError(film,
                "Должна появиться ошибка о неверной дате релиза",
                "releaseDate");
    }

    @Test
    void createFilmWithEmptyDurationShouldThrowException() throws Exception{
        Film film = Film.builder()
                .name("empty date")
                .description("description")
                .releaseDate(LocalDate.parse("2001-01-01"))
                .build();

        assertFieldValidationError(film,
                "Должна появиться ошибка об отсутствии продолжительности",
                "duration");
    }

    @Test
    void createFilmWithPastDurationShouldThrowException() throws Exception{
        Film film = Film.builder()
                .name("empty date")
                .description("description")
                .releaseDate(LocalDate.parse("2001-01-01"))
                .duration(-120)
                .build();

        assertFieldValidationError(film,
                "Должна появиться ошибка о неверной продолжительности",
                "duration");
    }

    @Test
    void putUser() throws Exception {
        Film film = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.parse("2001-01-01"))
                .duration(120)
                .build();

        JsonNode created = createdFilm(film);
        int filmId = created.get("id").asInt();

        Film filmToUpdate = Film.builder()
                .id(filmId)
                .name("Update name")
                .description("Update description")
                .releaseDate(LocalDate.parse("2011-01-01"))
                .duration(32)
                .build();

        MvcResult result = mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmToUpdate)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode updated = objectMapper.readTree(result.getResponse().getContentAsString());

        assertEquals(filmId, updated.get("id").asInt(),
                "У фильма должен был сохраниться ID = 1");
        assertEquals("Update name", updated.get("name").asText(),
                "Name должен совпадать с переданным в запросе");
        assertEquals("Update description", updated.get("description").asText(),
                "Description должен совпадать с переданным в запросе");
        assertEquals("2011-01-01", updated.get("releaseDate").asText(),
                "ReleaseDate должен совпадать с переданным в запросе");
        assertEquals("32", updated.get("duration").asText(),
                "Duration должен совпадать с переданным в запросе");
    }
}
