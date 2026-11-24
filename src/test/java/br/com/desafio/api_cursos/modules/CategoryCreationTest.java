package br.com.desafio.api_cursos.modules;

import br.com.desafio.api_cursos.modules.cursos.controller.CategoryController;
import br.com.desafio.api_cursos.modules.cursos.entity.CategoryEntity;
import br.com.desafio.api_cursos.modules.cursos.exception.GlobalExceptionHandler;
import br.com.desafio.api_cursos.modules.cursos.repository.CategoryRepository;
import br.com.desafio.api_cursos.modules.useCase.CreateCategoryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryCreationTest {

    private CategoryRepository categories;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        categories = mock(CategoryRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(new CategoryController(new CreateCategoryUseCase(categories)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsCategoryWithOriginalName() throws Exception {
        when(categories.insertIfAbsent(any(UUID.class), eq("Programação"))).thenReturn(1);
        when(categories.findById(any(UUID.class))).thenAnswer(invocation -> {
            var category = new CategoryEntity();
            category.setId(invocation.getArgument(0));
            category.setName("Programação");
            return Optional.of(category);
        });

        mvc.perform(post("/category").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Programação\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("Programação"));
    }

    @Test
    void rejectsDuplicateName() throws Exception {
        when(categories.insertIfAbsent(any(UUID.class), eq("Programação"))).thenReturn(0);

        mvc.perform(post("/category").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Programação\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void rejectsBlankAndOversizedNames() throws Exception {
        for (String name : new String[]{" ", "a".repeat(101)}) {
            mvc.perform(post("/category").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"" + name + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.name").isString());
        }
        verifyNoInteractions(categories);
    }
}
