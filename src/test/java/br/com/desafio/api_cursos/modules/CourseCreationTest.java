package br.com.desafio.api_cursos.modules;

import br.com.desafio.api_cursos.modules.cursos.controller.CourseController;
import br.com.desafio.api_cursos.modules.cursos.dto.CreateCourseRequest;
import br.com.desafio.api_cursos.modules.cursos.entity.CategoryEntity;
import br.com.desafio.api_cursos.modules.cursos.entity.CourseEntity;
import br.com.desafio.api_cursos.modules.cursos.exception.CourseAlreadyExistsException;
import br.com.desafio.api_cursos.modules.cursos.exception.GlobalExceptionHandler;
import br.com.desafio.api_cursos.modules.cursos.repository.CategoryRepository;
import br.com.desafio.api_cursos.modules.cursos.repository.CourseRepository;
import br.com.desafio.api_cursos.modules.useCase.CreateCourseUseCase;
import br.com.desafio.api_cursos.modules.useCase.FindCourseUseCase;
import br.com.desafio.api_cursos.modules.useCase.DeleteCourseUseCase;
import br.com.desafio.api_cursos.modules.useCase.UpdateCourseUseCase;
import br.com.desafio.api_cursos.modules.cursos.exception.CourseNotFoundException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CourseCreationTest {
    private CategoryRepository categories;
    private CourseRepository courses;
    private CreateCourseUseCase service;
    private CategoryEntity category;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        categories = mock(CategoryRepository.class);
        courses = mock(CourseRepository.class);
        service = new CreateCourseUseCase(categories, courses);
        category = new CategoryEntity();
        category.setId(UUID.randomUUID());
        category.setName("Programação");
        when(categories.findByName("Programação")).thenReturn(Optional.of(category));
        when(courses.saveAndFlush(any())).thenAnswer(invocation -> {
            CourseEntity course = invocation.getArgument(0);
            course.setId(UUID.randomUUID());
            return course;
        });
        mvc = MockMvcBuilders.standaloneSetup(new CourseController(service, new FindCourseUseCase(courses),
                        new UpdateCourseUseCase(courses, categories), new DeleteCourseUseCase(courses)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createsCourseWithNameAndCategory() throws Exception {
        mvc.perform(post("/course").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java\",\"category\":\"Programação\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.category").value("Programação"))
                .andExpect(jsonPath("$.active").value(true));
        verify(categories, never()).insertIfAbsent(any(), any());
    }

    @Test
    void createsMissingCategoryBeforeSavingCourse() {
        when(categories.findByName("Programação"))
                .thenReturn(Optional.empty(), Optional.of(category));
        var course = service.execute(new CreateCourseRequest("Java", "Programação"));
        verify(categories).insertIfAbsent(any(UUID.class), eq("Programação"));
        assertSame(category, course.getCategory());
    }

    @Test
    void returnsConflictForExistingCourse() throws Exception {
        when(courses.existsByNameAndCategory_Id("Java", category.getId())).thenReturn(true);
        mvc.perform(post("/course").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java\",\"category\":\"Programação\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
        verify(courses, never()).saveAndFlush(any());
    }

    @Test
    void translatesDatabaseDuplicateAfterConcurrentInsert() {
        var violation = new ConstraintViolationException("duplicate", new SQLException(),
                "uk_course_name_category");
        doThrow(new DataIntegrityViolationException("duplicate", violation)).when(courses).saveAndFlush(any());
        assertThrows(CourseAlreadyExistsException.class,
                () -> service.execute(new CreateCourseRequest("Java", "Programação")));
    }

    @Test
    void doesNotHideUnrelatedDatabaseFailures() {
        var failure = new DataIntegrityViolationException("other constraint");
        doThrow(failure).when(courses).saveAndFlush(any());
        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> service.execute(new CreateCourseRequest("Java", "Programação"))));
    }

    @Test
    void rejectsBlankName() throws Exception {
        mvc.perform(post("/course").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"category\":\"Programação\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categories, courses);
    }

    @Test
    void rejectsCategoryLongerThanDatabaseColumn() throws Exception {
        mvc.perform(post("/course").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java\",\"category\":\"" + "a".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categories, courses);
    }

    @Test
    void returnsNotFoundForMissingCourse() throws Exception {
        var id = UUID.randomUUID();
        when(courses.findById(id)).thenReturn(Optional.empty());
        mvc.perform(get("/course/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Curso não encontrado: " + id));
    }

    @Test
    void throwsDomainExceptionForMissingCourse() {
        var id = UUID.randomUUID();
        when(courses.findById(id)).thenReturn(Optional.empty());
        assertThrows(CourseNotFoundException.class, () -> new FindCourseUseCase(courses).execute(id));
    }

    @Test
    void returnsExistingCourseWithCategory() throws Exception {
        var id = UUID.randomUUID();
        var course = new CourseEntity();
        course.setId(id);
        course.setName("Java");
        course.setCategory(category);
        when(courses.findById(id)).thenReturn(Optional.of(course));
        mvc.perform(get("/course/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.category").value("Programação"));
    }
}
