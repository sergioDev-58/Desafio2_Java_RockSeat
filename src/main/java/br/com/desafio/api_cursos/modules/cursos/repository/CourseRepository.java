package br.com.desafio.api_cursos.modules.cursos.repository;

import br.com.desafio.api_cursos.modules.cursos.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    boolean existsByNameAndCategory_Id(String name, UUID categoryId);
    boolean existsByNameAndCategory_IdAndIdNot(String name, UUID categoryId, UUID id);
    Optional<CourseEntity> findAllById(UUID id);


}
