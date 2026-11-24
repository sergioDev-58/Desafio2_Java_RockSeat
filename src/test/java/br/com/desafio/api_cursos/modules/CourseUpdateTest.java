package br.com.desafio.api_cursos.modules;

import br.com.desafio.api_cursos.modules.cursos.controller.CourseController;
import br.com.desafio.api_cursos.modules.cursos.dto.CourseUpdateRequest;
import br.com.desafio.api_cursos.modules.cursos.entity.CategoryEntity;
import br.com.desafio.api_cursos.modules.cursos.entity.CourseEntity;
import br.com.desafio.api_cursos.modules.cursos.exception.*;
import br.com.desafio.api_cursos.modules.cursos.repository.*;
import br.com.desafio.api_cursos.modules.useCase.*;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CourseUpdateTest {
    private CourseRepository courses;
    private CategoryRepository categories;
    private UpdateCourseUseCase service;
    private CourseEntity course;
    private CategoryEntity category;
    private UUID id;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        courses = mock(CourseRepository.class);
        categories = mock(CategoryRepository.class);
        service = new UpdateCourseUseCase(courses, categories);
        id = UUID.randomUUID();
        category = new CategoryEntity();
        category.setId(UUID.randomUUID());
        category.setName("Programação");
        course = new CourseEntity();
        course.setId(id);
        course.setName("Java");
        course.setCategory(category);
        when(courses.findById(id)).thenReturn(Optional.of(course));
        when(courses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        mvc = MockMvcBuilders.standaloneSetup(new CourseController(
                new CreateCourseUseCase(categories, courses), new FindCourseUseCase(courses), service,
                new DeleteCourseUseCase(courses)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void updatesNameCategoryAndActiveThroughEndpoint() throws Exception {
        var replacement = new CategoryEntity();
        replacement.setId(UUID.randomUUID());
        replacement.setName("Backend");
        when(categories.findById(replacement.getId())).thenReturn(Optional.of(replacement));
        mvc.perform(put("/course/up/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Spring\",\"categoryId\":\"" + replacement.getId() + "\",\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Spring"))
                .andExpect(jsonPath("$.category").value("Backend"))
                .andExpect(jsonPath("$.active").value(false));
        assertSame(replacement, course.getCategory());
        verify(courses).saveAndFlush(course);
    }

    @Test
    void preservesOmittedFieldsAndSupportsFalse() throws Exception {
        mvc.perform(put("/course/up/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.category").value("Programação"))
                .andExpect(jsonPath("$.active").value(false));
        verifyNoInteractions(categories);
    }

    @Test
    void acceptsUnchangedCourseAndExcludesItFromDuplicateCheck() {
        var response = service.execute(id, new CourseUpdateRequest(null, null, null));
        assertEquals("Java", response.name());
        assertTrue(response.active());
        verify(courses).existsByNameAndCategory_IdAndIdNot("Java", category.getId(), id);
    }

    @Test
    void returnsNotFoundForMissingCourse() throws Exception {
        mvc.perform(put("/course/up/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
        verify(courses, never()).saveAndFlush(any());
    }

    @Test
    void returnsNotFoundForMissingCategory() throws Exception {
        mvc.perform(put("/course/up/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isNotFound());
        verify(courses, never()).saveAndFlush(any());
    }

    @Test
    void returnsConflictForDuplicate() throws Exception {
        when(courses.existsByNameAndCategory_IdAndIdNot("Spring", category.getId(), id)).thenReturn(true);
        mvc.perform(put("/course/up/{id}", id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Spring\"}"))
                .andExpect(status().isConflict());
        verify(courses, never()).saveAndFlush(any());
    }

    @Test
    void translatesConcurrentDuplicateAndPreservesOtherFailures() {
        var violation = new ConstraintViolationException("duplicate", new SQLException(), "uk_course_name_category");
        doThrow(new DataIntegrityViolationException("duplicate", violation)).when(courses).saveAndFlush(any());
        assertThrows(CourseAlreadyExistsException.class,
                () -> service.execute(id, new CourseUpdateRequest("Spring", null, null)));
        var failure = new DataIntegrityViolationException("other constraint");
        doThrow(failure).when(courses).saveAndFlush(any());
        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> service.execute(id, new CourseUpdateRequest("Spring", null, null))));
    }

    @Test
    void rejectsBlankLongNamesAndMalformedIds() throws Exception {
        for (String name : new String[]{"", "   ", "a".repeat(151)}) {
            mvc.perform(put("/course/up/{id}", id).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"" + name + "\"}"))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(put("/course/up/not-a-uuid").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(courses, categories);
    }
}
