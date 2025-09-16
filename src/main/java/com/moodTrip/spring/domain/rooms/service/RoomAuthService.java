package com.moodTrip.spring.domain.rooms.service;

import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import com.moodTrip.spring.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.moodTrip.spring.global.common.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
public class RoomAuthService {
    private final RoomMemberRepository roomMemberRepository;

    public boolean isActiveMember(Long roomId, Long memberPk) {
        return roomMemberRepository.existsByMember_MemberPkAndRoom_RoomIdAndIsActiveTrue(memberPk, roomId);
    }

    public void assertActiveMember(Long roomId, Long memberPk) {
        if (!isActiveMember(roomId, memberPk)) {
            throw new CustomException(ErrorStatus.FORBIDDEN);
        }
    }
}