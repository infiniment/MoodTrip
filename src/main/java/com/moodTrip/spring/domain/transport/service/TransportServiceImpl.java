package com.moodTrip.spring.domain.transport.service;

import com.moodTrip.spring.domain.transport.client.KakaoMapClient;
import com.moodTrip.spring.domain.transport.dto.response.KakaoAddressSearchResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoCoord2AddressResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoPlaceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class TransportServiceImpl implements TransportService {
    private final KakaoMapClient kakaoMapClient;

    @Override
    public KakaoPlaceResponse searchPlace(String query, Double x, Double y, Integer radius) {
        return kakaoMapClient.searchKeyword(query, x, y, radius, null, 1, 15);
    }

    @Override
    public KakaoAddressSearchResponse geocode(String address) {
        return kakaoMapClient.addressToCoord(address);
    }

    @Override
    public KakaoCoord2AddressResponse reverseGeocode(double x, double y) {
        return kakaoMapClient.coordToAddress(x, y);
    }
}
