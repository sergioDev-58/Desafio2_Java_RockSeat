package br.com.desafio.api_cursos.modules.useCase;

import br.com.desafio.api_cursos.modules.cursos.exception.CourseNotFoundException;
import br.com.desafio.api_cursos.modules.cursos.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCourseUseCase {

    private final CourseRepository courseRepository;

    @Transactional
    public void execute(UUID id) {
        var course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        courseRepository.delete(course);
    }
}
