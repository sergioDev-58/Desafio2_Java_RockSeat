package br.com.desafio.api_cursos.modules.cursos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "nome da categoria é obrigatório!") @Size(max = 100) String name

) {}
