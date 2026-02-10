package com.jutjubic.backend.repository;

import com.jutjubic.backend.entity.WatchParty;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WatchPartyRepository extends JpaRepository<WatchParty, Long> {
  Optional<WatchParty> findByRoomCode(String roomCode);

  @EntityGraph(attributePaths = {"creator"})
  @Query("select w from WatchParty w where w.roomCode = :roomCode")
  Optional<WatchParty> findWithCreatorByRoomCode(@Param("roomCode") String roomCode);
}
