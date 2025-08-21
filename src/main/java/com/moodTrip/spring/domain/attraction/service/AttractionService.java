package com.moodTrip.spring.domain.attraction.service;

import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionRegionResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.AttractionIntro;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface AttractionService {

    int syncAreaBasedList(int areaCode, Integer sigunguCode, Integer contentTypeId, int pageSize, long pauseMillis);
    int syncDetailIntro(long contentId, Integer contentTypeId);
    int syncDetailIntroByArea(int areaCode, Integer sigunguCode, Integer contentTypeId, long pauseMillis);

    Page<Attraction> searchKeywordPrefTitleStarts(String q, Integer area, Integer si, Integer type, int page, int size);

    // 페이지 응답
    AttractionRegionResponse getRegionAttractions(Integer areaCode, Integer sigunguCode, int page, int size);
    AttractionRegionResponse findAttractions(List<Integer> areaCodes, Pageable pageable);

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

   List<AttractionCardDTO> findAttractionsByEmotionTag(Integer tagId, int limit);

    // 카드/초기 로딩
    List<AttractionCardDTO> findAttractionsByEmotionIds(List<Integer> emotionIds);
    List<AttractionCardDTO> findInitialAttractions(int limit);

    // 기타
    List<Attraction> getAllAttractions();

    AttractionRegionResponse findAttractionsFiltered(
            List<String> regionCodes, // "KR11" 같은 코드
            Pageable pageable,
            String keyword,
            String cat1,
            String cat2,
            String cat3,
            String sort
    );

    Optional<AttractionResponse> getDetail(long contentId);

    AttractionIntro getIntro(long contentId, Integer contentTypeId);
    AttractionDetailResponse getDetailResponse(long contentId);

}
