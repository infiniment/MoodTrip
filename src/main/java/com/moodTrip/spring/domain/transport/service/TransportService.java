package com.moodTrip.spring.domain.transport.service;

import com.moodTrip.spring.domain.transport.dto.response.KakaoAddressSearchResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoCoord2AddressResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoPlaceResponse;

public interface TransportService {
    KakaoPlaceResponse searchPlace(String query, Double x, Double y, Integer radius);
    KakaoAddressSearchResponse geocode(String address);
    KakaoCoord2AddressResponse reverseGeocode(double x, double y);
}