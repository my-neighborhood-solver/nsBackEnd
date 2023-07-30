package com.zerobase.nsbackend.chatting.domain.service;

import com.zerobase.nsbackend.chatting.domain.entitiy.ChattingContent;
import com.zerobase.nsbackend.chatting.domain.entitiy.ChattingRoom;
import com.zerobase.nsbackend.chatting.domain.repository.ChattingContentRepository;
import com.zerobase.nsbackend.chatting.domain.repository.ChattingRoomRepository;
import com.zerobase.nsbackend.chatting.dto.ChattingRoomAllResponse;
import com.zerobase.nsbackend.chatting.dto.ChattingRoomCreateResponse;
import com.zerobase.nsbackend.errand.domain.Errand;
import com.zerobase.nsbackend.errand.domain.ErrandService;
import com.zerobase.nsbackend.global.exceptionHandle.ErrorCode;
import com.zerobase.nsbackend.member.domain.Member;
import com.zerobase.nsbackend.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChattingRoomService {

  private final ChattingRoomRepository chattingRoomRepository;
  private final ChattingContentRepository chattingContentRepository;
  private final MemberRepository memberRepository;

  private final ErrandService errandService;


  // 채팅방 생성
  @Transactional
  public ResponseEntity<ChattingRoomCreateResponse> createChattingRoom(Long errand_id,
      Long sender_id) {
    // 유효성 검사 member_Id 가 있는지 errand_Id가 있는지
    Errand errand = errandService.getErrand(errand_id);

    // member 있는지 검사
    Member sender = memberFindById(sender_id);

    ChattingRoom chattingRoom = chattingRoomRepository
        .findByErrand_MemberAndSender(errand, sender);
    if (chattingRoom != null) {
      // 이미 채팅방이 존재한다
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ChattingRoomCreateResponse.from(chattingRoom));

    }

    ChattingRoom ChattingRoomSave = chattingRoomRepository.save(
        ChattingRoom.builder()
            .errand(errand)
            .sender(sender)
            .build());

    return ResponseEntity.ok().body(ChattingRoomCreateResponse.from(ChattingRoomSave));
  }


  // 채팅방 전체 조회
  @Transactional
  public List<ChattingRoomAllResponse> getChattingRoomsByMemberId(Long memberId) {

    // 유효성 검사 memberId가 있는지
    Member sender = memberFindById(memberId);

    List<ChattingRoom> chattingRooms = chattingRoomRepository
        .findByErrandMemberIdAndSenderMemberId(memberId, memberId);

    List<ChattingRoomAllResponse> chattingRoomResponses = new ArrayList<>();

    for (ChattingRoom chattingRoom : chattingRooms) {
      Long id = chattingRoom.getId();
      List<ChattingContent> chattingContent = chattingRoom.getChattingContent();
      String content = null;
      LocalDateTime time = null;

      Long isRead = chattingContentRepository.countBySenderNotAndIsRead(sender, false);

      if (!chattingContent.isEmpty()) {
        content = chattingContent.get(chattingContent.size() - 1).getContent();
        time = chattingContent.get(chattingContent.size() - 1).getContentTime();
      }

      chattingRoomResponses.add(ChattingRoomAllResponse.from(chattingRoom, content, time, isRead));
    }   // for each 문 끝
    Collections.sort(chattingRoomResponses, new Comparator<ChattingRoomAllResponse>() {
      @Override
      public int compare(ChattingRoomAllResponse o1, ChattingRoomAllResponse o2) {
        return o2.getTime().compareTo(o1.getTime());
      }
    });

    return chattingRoomResponses;
  }


  // 채팅방 단건조회
  @Transactional
  public List<ChattingContent> getChattingRoomByIdAndMemberId(Long roomId, Long memberId) {

    // 채팅방 존재하는지 멤버가 존재하는지 검사
//    if(!ChattingRoomValidationUtil(roomId, memberId)){
//      //채팅방 안에 멤버가 존재하지않는다.
//      throw new IllegalArgumentException(ErrorCode.CHATTING_NOT_FOUND.getDescription());
//    }

    return chattingContentRepository
        .findByChattingRoomIdOrderByContentTimeDesc(roomId);

  }

  // member가 있는지 유효성 검사
  private Member memberFindById(Long memberId) {
    Optional<Member> member = memberRepository.findById(memberId);
    if (member.isPresent()) {
      return member.get();
    }
    throw new IllegalArgumentException(ErrorCode.MEMBER_NOT_FOUND.getDescription());
  }

  // 채팅방이 존재하는지 유효성 검사
  private ChattingRoom chattingRoomFindById(Long roomId) {
    Optional<ChattingRoom> chattingRoom = chattingRoomRepository.findById(roomId);
    if (chattingRoom.isPresent()) {
      return chattingRoom.get();
    }
    throw new IllegalArgumentException(ErrorCode.CHATTING_NOT_FOUND.getDescription());
  }

  // 채팅방 안에 멤버가 있는지 유효성 검사
//  private boolean ChattingRoomValidationUtil(Long roomId, Long memberId) {
//    Member member = memberFindById(memberId);
//    ChattingRoom room = chattingRoomFindById(roomId);
//    if(member == room.getSender() || member == room.getErrand().getMember()) {
//      return true;
//    }
//
//    return false;
//  }
}
