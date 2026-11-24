package br.com.desafio.api_cursos.modules.cursos.dto;

import br.com.desafio.api_cursos.modules.cursos.entity.CourseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String name,
        String category,
        boolean active,
        LocalDateTime createAt

) {
    public static CourseResponse from(CourseEntity course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getCategory().getName(),
                course.getActive(),
                course.getCreatedAt()


        );
    }
}
