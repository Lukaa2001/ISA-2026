package com.jutjubic.backend.repository;

import com.jutjubic.backend.entity.Video;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, Long> {
  @EntityGraph(attributePaths = {"user"})
  Page<Video> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  Page<Video> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  @Query("select v from Video v where v.id = :id")
  Optional<Video> findDetailsById(@Param("id") Long id);

  long countByUserId(Long userId);
}
