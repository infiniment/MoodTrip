package com.moodTrip.spring.domain.attraction.service;

import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;

import java.util.List;
import java.util.Set;

public interface AttractionService {
    int syncAreaBasedList(int areaCode, Integer sigunguCode, Integer contentTypeId, int pageSize, long pauseMillis);
    int syncDetailIntro(long contentId, Integer contentTypeId);
    int syncDetailIntroByArea(int areaCode, Integer sigunguCode, Integer contentTypeId, long pauseMillis);

    List<AttractionResponse> findByRegionCodes(List<String> regionCodes, String sort);
    List<Attraction> find(int areaCode, Integer sigunguCode, Integer contentTypeId);

    AttractionResponse create(AttractionInsertRequest req);

    default int syncAreaBasedListExcluding(int areaCode, Integer sigunguCode, Integer contentTypeId,
                                           int pageSize, long pauseMillis, Set<Integer> excludeContentTypeIds) {
        return syncAreaBasedList(areaCode, sigunguCode, contentTypeId, pageSize, pauseMillis);
    }
}
