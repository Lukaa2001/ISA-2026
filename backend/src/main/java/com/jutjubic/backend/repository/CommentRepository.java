package com.jutjubic.backend.repository;

import com.jutjubic.backend.entity.Comment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @EntityGraph(attributePaths = {"user"})
  Page<Comment> findByVideoIdOrderByCreatedAtDesc(Long videoId, Pageable pageable);

  long countByVideoId(Long videoId);

  long countByUserId(Long userId);

  @EntityGraph(attributePaths = {"user"})
  List<Comment> findByVideoId(Long videoId);
}
