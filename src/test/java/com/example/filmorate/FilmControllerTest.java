package com.example.filmorate;

import com.example.filmorate.controller.FilmController;
import com.example.filmorate.controller.UserController;
import com.example.filmorate.model.Film;
import com.example.filmorate.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
