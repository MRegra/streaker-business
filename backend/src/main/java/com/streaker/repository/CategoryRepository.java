package com.streaker.repository;

import com.streaker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserUuid(UUID userUuid);

    @Query(
            value = "SELECT * FROM categories WHERE user_uuid = :userUuid AND uuid = :categoryUuid",
            nativeQuery = true
    )
    Optional<Category> findByUserUuidAndUuid(@Param("userUuid") UUID userUuid, @Param("categoryUuid") UUID categoryUuid);
}
