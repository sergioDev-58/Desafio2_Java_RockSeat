package br.com.desafio.api_cursos.modules.useCase;

import br.com.desafio.api_cursos.modules.cursos.dto.CourseResponse;
import br.com.desafio.api_cursos.modules.cursos.dto.CourseUpdateRequest;
import br.com.desafio.api_cursos.modules.cursos.exception.CourseNotFoundException;
import br.com.desafio.api_cursos.modules.cursos.exception.CategoryNotFoundException;
import br.com.desafio.api_cursos.modules.cursos.exception.CourseAlreadyExistsException;
import br.com.desafio.api_cursos.modules.cursos.repository.CategoryRepository;
import br.com.desafio.api_cursos.modules.cursos.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCourseUseCase {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public CourseResponse execute(UUID id, CourseUpdateRequest request) {
        var course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        var category = request.categoryId() == null ? course.getCategory()
                : categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        var name = request.name() == null ? course.getName() : request.name();
        if (courseRepository.existsByNameAndCategory_IdAndIdNot(name, category.getId(), id)) {
            throw new CourseAlreadyExistsException();
        }
        course.update(request.name(), category, request.active());
        try {
            return CourseResponse.from(courseRepository.saveAndFlush(course));
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
