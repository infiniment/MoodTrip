package com.moodTrip.spring.domain.attraction.service;

import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;

import java.util.List;

public interface AttractionService {
    int syncAreaBasedList(int areaCode, Integer sigunguCode, Integer contentTypeId, int pageSize, long pauseMillis);
    int syncDetailIntro(long contentId, Integer contentTypeId);
    int syncDetailIntroByArea(int areaCode, Integer sigunguCode, Integer contentTypeId, long pauseMillis);

    // 조회
    List<Attraction> find(int areaCode, Integer sigunguCode, Integer contentTypeId);

    // 수동 등록
    AttractionResponse create(AttractionInsertRequest req);
}
