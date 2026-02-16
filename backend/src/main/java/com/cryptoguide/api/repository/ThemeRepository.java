package com.cryptoguide.api.repository;

import com.cryptoguide.api.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Optional<Theme> findBySlug(String slug);

    Optional<Theme> findByName(String name);

    List<Theme> findAllByOrderByNameAsc();

    boolean existsBySlug(String slug);
}
