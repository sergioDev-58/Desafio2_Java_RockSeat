package br.com.desafio.api_cursos.modules;

import br.com.desafio.api_cursos.modules.cursos.controller.CourseController;
import br.com.desafio.api_cursos.modules.cursos.entity.CourseEntity;
import br.com.desafio.api_cursos.modules.cursos.exception.GlobalExceptionHandler;
import br.com.desafio.api_cursos.modules.cursos.repository.CategoryRepository;
import br.com.desafio.api_cursos.modules.cursos.repository.CourseRepository;
import br.com.desafio.api_cursos.modules.useCase.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CourseDeletionTest {
    private CourseRepository courses;
    private CategoryRepository categories;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        courses = mock(CourseRepository.class);
        categories = mock(CategoryRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(new CourseController(
                        new CreateCourseUseCase(categories, courses), new FindCourseUseCase(courses),
                        new UpdateCourseUseCase(courses, categories), new DeleteCourseUseCase(courses)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void deletesExistingCourseAndReturnsNoContent() throws Exception {
        var id = UUID.randomUUID();
        var course = new CourseEntity();
        course.setId(id);
        when(courses.findById(id)).thenReturn(Optional.of(course));

        mvc.perform(delete("/course/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(courses).delete(course);
        verifyNoInteractions(categories);
    }

    @Test
    void returnsNotFoundForMissingCourse() throws Exception {
        var id = UUID.randomUUID();
        when(courses.findById(id)).thenReturn(Optional.empty());

        mvc.perform(delete("/course/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Curso não encontrado: " + id));

        verify(courses, never()).delete(any(CourseEntity.class));
        verifyNoInteractions(categories);
    }

    @Test
    void rejectsMalformedId() throws Exception {
        mvc.perform(delete("/course/not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courses, categories);
    }
}
