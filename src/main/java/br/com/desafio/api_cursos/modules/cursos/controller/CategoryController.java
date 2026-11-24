package br.com.desafio.api_cursos.modules.cursos.controller;

import br.com.desafio.api_cursos.modules.cursos.dto.CategoryResponse;
import br.com.desafio.api_cursos.modules.cursos.dto.CreateCategoryRequest;
import br.com.desafio.api_cursos.modules.useCase.CreateCategoryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CreateCategoryRequest request) {
        var category = createCategoryUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(category));
    }
}
