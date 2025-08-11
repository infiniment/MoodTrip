package com.moodTrip.spring.domain.transport.client;

import com.moodTrip.spring.domain.transport.dto.response.KakaoPlaceResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoAddressSearchResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoCoord2AddressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoMapClient {
    private final RestTemplate restTemplate;

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    private static final String BASE = "https://dapi.kakao.com";

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "KakaoAK " + restApiKey);
        return h;
    }

    // 키워드 검색
    public KakaoPlaceResponse searchKeyword(String query, Double x, Double y,
                                            Integer radius, String categoryGroupCode,
                                            Integer page, Integer size) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl(BASE + "/v2/local/search/keyword.json")
                .queryParam("query", query);
        if (x != null && y != null) { b.queryParam("x", x).queryParam("y", y); }
        if (radius != null)        { b.queryParam("radius", radius); }        // 0~20000m
        if (categoryGroupCode != null) { b.queryParam("category_group_code", categoryGroupCode); }
        if (page != null)          { b.queryParam("page", page); }
        if (size != null)          { b.queryParam("size", size); }

        HttpEntity<Void> req = new HttpEntity<>(headers());
        ResponseEntity<KakaoPlaceResponse> res =
                restTemplate.exchange(b.build(true).toUri(), HttpMethod.GET, req, KakaoPlaceResponse.class);
        return res.getBody();
    }

    // 주소 → 좌표(지오코딩)
    public KakaoAddressSearchResponse addressToCoord(String address) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl(BASE + "/v2/local/search/address.json")
                .queryParam("query", address);

        HttpEntity<Void> req = new HttpEntity<>(headers());
        return restTemplate.exchange(b.build(true).toUri(), HttpMethod.GET, req,
                KakaoAddressSearchResponse.class).getBody();
    }

    // 좌표 → 도로명/지번주소(리버스 지오코딩)
    public KakaoCoord2AddressResponse coordToAddress(double x, double y) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl(BASE + "/v2/local/geo/coord2address.json")
                .queryParam("x", x)
                .queryParam("y", y);

        HttpEntity<Void> req = new HttpEntity<>(headers());
        return restTemplate.exchange(b.build(true).toUri(), HttpMethod.GET, req,
                KakaoCoord2AddressResponse.class).getBody();
    }
}
