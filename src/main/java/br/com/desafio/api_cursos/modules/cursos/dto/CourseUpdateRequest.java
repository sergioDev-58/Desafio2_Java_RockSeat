package br.com.desafio.api_cursos.modules.cursos.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CourseUpdateRequest(
   @Size(min = 1, max = 150)
   @Pattern(regexp = "(?s).*\\S.*", message = "não deve estar em branco") String name, // null = não alterar.
   UUID categoryId,
   Boolean active
) {}
