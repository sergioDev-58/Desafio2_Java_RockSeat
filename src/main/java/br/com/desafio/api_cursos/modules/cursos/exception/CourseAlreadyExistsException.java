package br.com.desafio.api_cursos.modules.cursos.exception;

public class CourseAlreadyExistsException extends RuntimeException {
    public CourseAlreadyExistsException() {
        super("Já existe um curso com esse nome nessa categoria");
    }
}
