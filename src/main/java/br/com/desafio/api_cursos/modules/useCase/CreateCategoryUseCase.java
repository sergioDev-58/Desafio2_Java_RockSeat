package br.com.desafio.api_cursos.modules.useCase;

import br.com.desafio.api_cursos.modules.cursos.dto.CreateCategoryRequest;
import br.com.desafio.api_cursos.modules.cursos.entity.CategoryEntity;
import br.com.desafio.api_cursos.modules.cursos.exception.CategoryAlreadyExistsException;
import br.com.desafio.api_cursos.modules.cursos.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryEntity execute(CreateCategoryRequest request) {
        var id = UUID.randomUUID();
        if (categoryRepository.insertIfAbsent(id, request.name()) == 0) {
            throw new CategoryAlreadyExistsException();
        }
        return categoryRepository.findById(id).orElseThrow();
    }
}
