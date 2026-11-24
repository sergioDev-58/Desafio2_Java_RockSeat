package br.com.desafio.api_cursos.modules.useCase;

import br.com.desafio.api_cursos.modules.cursos.dto.CourseResponse;
import br.com.desafio.api_cursos.modules.cursos.exception.CourseNotFoundException;
import br.com.desafio.api_cursos.modules.cursos.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FindCourseUseCase {
    private final CourseRepository courseRepository;

    public FindCourseUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public CourseResponse execute(UUID id) {
        var course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        return CourseResponse.from(course);
    }
}
