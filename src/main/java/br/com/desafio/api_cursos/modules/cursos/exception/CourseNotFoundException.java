package br.com.desafio.api_cursos.modules.cursos.exception;

import java.util.UUID;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(UUID id) {
        super("Curso não encontrado: " + id);
    }
}
