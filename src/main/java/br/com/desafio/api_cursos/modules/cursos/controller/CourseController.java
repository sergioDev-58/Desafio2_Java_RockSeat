package br.com.desafio.api_cursos.modules.cursos.controller;

import br.com.desafio.api_cursos.modules.cursos.dto.CourseResponse;
import br.com.desafio.api_cursos.modules.cursos.dto.CourseUpdateRequest;
import br.com.desafio.api_cursos.modules.cursos.dto.CreateCourseRequest;
import br.com.desafio.api_cursos.modules.useCase.UpdateCourseUseCase;
import br.com.desafio.api_cursos.modules.useCase.CreateCourseUseCase;
import br.com.desafio.api_cursos.modules.useCase.FindCourseUseCase;
import br.com.desafio.api_cursos.modules.useCase.DeleteCourseUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class CourseController {
    private final CreateCourseUseCase createCursoUseCase;

    private final FindCourseUseCase findCursoUseCase;
    private final UpdateCourseUseCase courseUpdateUseCase;
    private final DeleteCourseUseCase deleteCourseUseCase;

    public CourseController(CreateCourseUseCase createCursoUseCase, FindCourseUseCase findCursoUseCase,
                            UpdateCourseUseCase courseUpdateUseCase, DeleteCourseUseCase deleteCourseUseCase) {
        this.createCursoUseCase = createCursoUseCase;
        this.findCursoUseCase = findCursoUseCase;
        this.courseUpdateUseCase = courseUpdateUseCase;
        this.deleteCourseUseCase = deleteCourseUseCase;
    }

    @GetMapping("/course/{id}")
    public ResponseEntity<CourseResponse> findById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(findCursoUseCase.execute(id));
    }

    @PostMapping("/course")
    public ResponseEntity<CourseResponse> create(@RequestBody @Valid CreateCourseRequest request) {
        var course = createCursoUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseResponse.from(course));
    }

    @PutMapping("/course/up/{id}")
    public ResponseEntity<CourseResponse> update(@PathVariable("id") UUID id, @RequestBody @Valid CourseUpdateRequest request) {
        return ResponseEntity.ok(courseUpdateUseCase.execute(id, request));
    }

    @DeleteMapping("/course/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        deleteCourseUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
