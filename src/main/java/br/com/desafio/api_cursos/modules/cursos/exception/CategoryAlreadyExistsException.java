package br.com.desafio.api_cursos.modules.cursos.exception;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException() {
        super("Já existe uma categoria com esse nome.");
    }
}
