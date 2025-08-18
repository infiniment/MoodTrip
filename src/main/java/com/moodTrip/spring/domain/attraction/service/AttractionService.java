package com.moodTrip.spring.domain.attraction.service;

import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface AttractionService {
    int syncAreaBasedList(int areaCode, Integer sigunguCode, Integer contentTypeId, int pageSize, long pauseMillis);
    int syncDetailIntro(long contentId, Integer contentTypeId);
    int syncDetailIntroByArea(int areaCode, Integer sigunguCode, Integer contentTypeId, long pauseMillis);

    Page<Attraction> searchKeywordPrefTitleStarts(String q, Integer area, Integer si, Integer type, int page, int size);

    // 조회
    List<AttractionResponse> findByRegionCodes(List<String> regionCodes, String sort);
    List<Attraction> find(int areaCode, Integer sigunguCode, Integer contentTypeId);

    // 수동 등록
    AttractionResponse create(AttractionInsertRequest req);

    default int syncAreaBasedListExcluding(int areaCode, Integer sigunguCode, Integer contentTypeId,
                                           int pageSize, long pauseMillis, Set<Integer> excludeContentTypeIds) {
        return syncAreaBasedList(areaCode, sigunguCode, contentTypeId, pageSize, pauseMillis);
    }

    //페이징

    Page<Attraction> findAttractions(int page, int size);

    //관리자 감정 매핑 검색
    Page<Attraction> searchAttractions(String keyword, int page, int size);
}
