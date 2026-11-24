package br.com.desafio.api_cursos.modules.cursos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank(message = "nome do curso é obrigatório!") @Size(max = 150) String name,
        @NotBlank(message = "categoria do curso é obrigatória!") @Size(max = 100) String category
) {}
