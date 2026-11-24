package br.com.desafio.api_cursos.modules.cursos.dto;

import br.com.desafio.api_cursos.modules.cursos.entity.CategoryEntity;
import br.com.desafio.api_cursos.modules.cursos.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryResolver {
    private final CategoryRepository categoryRepository;

    public CategoryEntity findOrCreate(String name) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            categoryRepository.insertIfAbsent(UUID.randomUUID(), name);
            return categoryRepository.findByName(name).orElseThrow();
        });
    }
}
