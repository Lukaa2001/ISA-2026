package com.jutjubic.backend.repository;

import com.jutjubic.backend.entity.WatchPartyMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchPartyMemberRepository extends JpaRepository<WatchPartyMember, Long> {
  Optional<WatchPartyMember> findByUserIdAndWatchPartyId(Long userId, Long watchPartyId);

  @EntityGraph(attributePaths = {"user"})
  List<WatchPartyMember> findByWatchPartyId(Long watchPartyId);
}
