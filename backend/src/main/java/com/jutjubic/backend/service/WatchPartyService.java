package com.jutjubic.backend.service;

import com.jutjubic.backend.entity.User;
import com.jutjubic.backend.entity.WatchParty;
import com.jutjubic.backend.entity.WatchPartyMember;
import com.jutjubic.backend.exception.ApiException;
import com.jutjubic.backend.repository.UserRepository;
import com.jutjubic.backend.repository.WatchPartyMemberRepository;
import com.jutjubic.backend.repository.WatchPartyRepository;
import com.jutjubic.backend.util.ResponseMapper;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WatchPartyService {
  private final WatchPartyRepository watchPartyRepository;
  private final WatchPartyMemberRepository watchPartyMemberRepository;
  private final UserRepository userRepository;
  private final SecureRandom random = new SecureRandom();

  public WatchPartyService(
      WatchPartyRepository watchPartyRepository,
      WatchPartyMemberRepository watchPartyMemberRepository,
      UserRepository userRepository
  ) {
    this.watchPartyRepository = watchPartyRepository;
    this.watchPartyMemberRepository = watchPartyMemberRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public Map<String, Object> create(long userId) {
    User creator = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(404, "User not found"));

    WatchParty party = new WatchParty();
    party.setRoomCode(generateUniqueRoomCode());
    party.setCreator(creator);
    party.setActive(true);

    WatchParty savedParty = watchPartyRepository.save(party);

    WatchPartyMember member = new WatchPartyMember();
    member.setUser(creator);
    member.setWatchParty(savedParty);
    watchPartyMemberRepository.save(member);

    return buildPartyResponse(savedParty.getRoomCode());
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getRoom(String roomCode) {
    WatchParty party = watchPartyRepository.findWithCreatorByRoomCode(roomCode)
        .orElseThrow(() -> new ApiException(404, "Watch party not found or inactive"));

    if (!party.isActive()) {
      throw new ApiException(404, "Watch party not found or inactive");
    }

    List<WatchPartyMember> members = watchPartyMemberRepository.findByWatchPartyId(party.getId());
    return ResponseMapper.mapWatchParty(party, members);
  }

  @Transactional
  public Map<String, Object> join(String roomCode, long userId) {
    WatchParty party = watchPartyRepository.findWithCreatorByRoomCode(roomCode)
        .orElseThrow(() -> new ApiException(404, "Watch party not found or inactive"));

    if (!party.isActive()) {
      throw new ApiException(404, "Watch party not found or inactive");
    }

    WatchPartyMember existing = watchPartyMemberRepository.findByUserIdAndWatchPartyId(userId, party.getId()).orElse(null);
    if (existing == null) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ApiException(404, "User not found"));

      WatchPartyMember member = new WatchPartyMember();
      member.setUser(user);
      member.setWatchParty(party);
      watchPartyMemberRepository.save(member);
    }

    return buildPartyResponse(roomCode);
  }

  @Transactional
  public Map<String, Object> close(String roomCode, long userId) {
    WatchParty party = watchPartyRepository.findByRoomCode(roomCode)
        .orElseThrow(() -> new ApiException(404, "Watch party not found"));

    if (!party.getCreator().getId().equals(userId)) {
      throw new ApiException(403, "Only the creator can close the watch party");
    }

    party.setActive(false);
    watchPartyRepository.save(party);

    return Map.of("message", "Watch party closed");
  }

  private Map<String, Object> buildPartyResponse(String roomCode) {
    WatchParty updatedParty = watchPartyRepository.findWithCreatorByRoomCode(roomCode)
        .orElseThrow(() -> new ApiException(404, "Watch party not found or inactive"));
    List<WatchPartyMember> members = watchPartyMemberRepository.findByWatchPartyId(updatedParty.getId());
    return ResponseMapper.mapWatchParty(updatedParty, members);
  }

  private String generateUniqueRoomCode() {
    for (int i = 0; i < 10; i++) {
      byte[] bytes = new byte[4];
      random.nextBytes(bytes);
      String code = bytesToHex(bytes).toUpperCase();
      if (watchPartyRepository.findByRoomCode(code).isEmpty()) {
        return code;
      }
    }

    throw new ApiException(500, "Failed to generate room code");
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
