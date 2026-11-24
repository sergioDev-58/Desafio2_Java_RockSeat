package br.com.desafio.api_cursos.modules.cursos.repository;

import br.com.desafio.api_cursos.modules.cursos.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    Optional<CategoryEntity> findByName(String name);

    @Modifying
    @Query(value = "INSERT INTO categories (id, name) VALUES (:id, :name) ON CONFLICT (name) DO NOTHING",
            nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("name") String name);
}
