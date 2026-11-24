package br.com.desafio.api_cursos.modules.cursos.dto;

import br.com.desafio.api_cursos.modules.cursos.entity.CategoryEntity;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name
) {

    public static CategoryResponse from(CategoryEntity category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
