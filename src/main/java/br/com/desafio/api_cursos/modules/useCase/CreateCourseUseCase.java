package br.com.desafio.api_cursos.modules.useCase;

import br.com.desafio.api_cursos.modules.cursos.dto.CreateCourseRequest;
import br.com.desafio.api_cursos.modules.cursos.entity.CourseEntity;
import br.com.desafio.api_cursos.modules.cursos.exception.CourseAlreadyExistsException;
import br.com.desafio.api_cursos.modules.cursos.repository.CategoryRepository;
import br.com.desafio.api_cursos.modules.cursos.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCourseUseCase {
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public CourseEntity execute(CreateCourseRequest request) {
        var category = categoryRepository.findByName(request.category()).orElseGet(() -> {
            // O PostgreSQL resolve a disputa entre cadastros simultâneos da mesma categoria.
            categoryRepository.insertIfAbsent(UUID.randomUUID(), request.category());
            return categoryRepository.findByName(request.category()).orElseThrow();
        });

        if (courseRepository.existsByNameAndCategory_Id(request.name(), category.getId())) {
            throw new CourseAlreadyExistsException();
        }

        var course = new CourseEntity();
        course.setName(request.name());
        course.setCategory(category);

        try {
            // Faz a restrição única ser verificada antes de sair do serviço.
            return courseRepository.saveAndFlush(course);
        } catch (DataIntegrityViolationException ex) {
            for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "uk_course_name_category".equals(violation.getConstraintName())) {
                    throw new CourseAlreadyExistsException();
                }
            }
            throw ex;
        }
    }
}
