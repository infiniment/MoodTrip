package com.moodTrip.spring.domain.attraction.service;

public interface AttractionService {
    int fetchAndSaveAttractions(int areaCode, int contentTypeId);
}
