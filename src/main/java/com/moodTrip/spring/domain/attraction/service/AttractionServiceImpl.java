package com.moodTrip.spring.domain.attraction.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository attractionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    // yml에서 주입받는 인코딩된 API 키
    @Value("${attraction.apikey.encoding}")
    private String apiKey;

    // KorService1 Base URL
    private static final String BASE_URL        = "https://apis.data.go.kr/B551011/KorService1";
    private static final String AREA_BASED_LIST = "/areaBasedList1";
    private static final String DETAIL_COMMON   = "/detailCommon1";
    private static final String DETAIL_INTRO    = "/detailIntro1";
    private static final String DETAIL_IMAGE    = "/detailImage1";

    @Override
    @Transactional
    public int fetchAndSaveAttractions(int areaCode, int contentTypeId) {
        // 1) 지역 기반 목록 호출
        JsonNode listRoot = call(buildAreaListUri(areaCode, contentTypeId));
        JsonNode listItems = listRoot.path("response").path("body").path("items").path("item");

        if (!listItems.isArray() || listItems.size() == 0) return 0;

        int affected = 0;

        for (JsonNode li : listItems) {
            String contentId   = li.path("contentid").asText(null);
            String typeId      = li.path("contenttypeid").asText(null);

            // 2) 상세 호출 (공통/이미지) - detailIntro1 호출은 삭제
            JsonNode common = firstItem(call(buildDetailCommonUri(contentId, typeId)));
            // JsonNode intro  = firstItem(call(buildDetailIntroUri(contentId, typeId))); // intro 호출 삭제
            JsonNode imgs   = call(buildDetailImageUri(contentId, typeId));

            // 3) 공통/소개에서 페이지에 필요한 필드만 추출
            String title      = text(li, "title");
            String firstimage = text(common, "firstimage");
            String addr1      = text(common, "addr1");
            String addr2      = text(common, "addr2");
            String tel        = text(common, "tel");
            String overview   = text(common, "overview");

            // intro 호출을 삭제했으므로 관련 변수도 주석 처리 또는 초기화
            String useTime    = null; // text(intro, "usetime");
            String restDate   = null; // text(intro, "restdate");
            String parking    = null; // text(intro, "parking");
            String expAge     = null; // text(intro, "expagerange");

            // 4) 대표 이미지 보강: firstimage가 없으면 이미지 목록 첫 장 사용
            if ((firstimage == null || firstimage.isBlank())) {
                JsonNode imgItems = imgs.path("response").path("body").path("items").path("item");
                if (imgItems.isArray() && imgItems.size() > 0) {
                    firstimage = imgItems.get(0).path("originimgurl").asText(null);
                }
            }

            // 5) 업서트 (contentId 기준)
            Attraction entity = attractionRepository.findByContentId(contentId)
                    .orElseGet(() -> Attraction.builder().contentId(contentId).build());

            entity.setContentTypeId(typeId);
            entity.setTitle(title);
            entity.setThumbnail(firstimage);
            entity.setTel(tel);
            entity.setAddr1(addr1);
            entity.setAddr2(addr2);
            // intro 호출을 삭제했으므로 관련 필드 설정도 주석 처리
            // entity.setUseTime(useTime);
            // entity.setRestDate(restDate);
            // entity.setParking(parking);
            // entity.setExpAgeRange(expAge);
            entity.setOverview(overview);

            // 좌표/지역코드: 목록 응답 기준
            entity.setMapX(safeDouble(li.path("mapx").asText(null)));
            entity.setMapY(safeDouble(li.path("mapy").asText(null)));
            entity.setAreaCode(text(li, "areacode"));
            entity.setSigunguCode(text(li, "sigungucode"));

            attractionRepository.save(entity);
            affected++;
        }
        return affected;
    }

    /* ----------------- HTTP & URI ----------------- */

    // 예시: buildAreaListUri 메서드
    private URI buildAreaListUri(int areaCode, int contentTypeId) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL + AREA_BASED_LIST)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("serviceKey", apiKey)
                .queryParam("numOfRows", 200)
                .queryParam("pageNo", 1)
                .queryParam("areaCode", areaCode)
                .queryParam("contentTypeId", contentTypeId)
                .build() // build(false) 대신 build()를 사용합니다.
                .toUri();
    }
// 다른 build...Uri 메서드도 동일하게 수정합니다.

    private URI buildDetailCommonUri(String contentId, String contentTypeId) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL + "/detailCommon1")
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("serviceKey", apiKey)
                .queryParam("defaultYN", "Y")
                .queryParam("firstImageYN", "Y")
                .queryParam("addrinfoYN", "Y")
                .queryParam("overviewYN", "Y")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build() // ✅ 이중 인코딩 방지를 위해 false로 설정
                .toUri();
    }

    private URI buildDetailIntroUri(String contentId, String contentTypeId) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL + "/detailIntro1")
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("serviceKey", apiKey)
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build() // ✅ 이중 인코딩 방지를 위해 false로 설정
                .toUri();
    }

    private URI buildDetailImageUri(String contentId, String contentTypeId) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL + "/detailImage1")
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("serviceKey", apiKey)
                .queryParam("contentId", contentId)
                .queryParam("imageYN", "Y")
                .queryParam("subImageYN", "Y")
                .queryParam("numOfRows", 50)
                .build() // ✅ 이중 인코딩 방지를 위해 false로 설정
                .toUri();
    }

    private JsonNode call(URI uri) {
        try {
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .header("Accept", "application/json")
                    .header("User-Agent", "moodTrip/1.0")
                    .GET().build();


            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            // 상태코드 확인
            if (res.statusCode() != 200) {
                throw new RuntimeException("HTTP " + res.statusCode() + " : " + uri + "\n" + head(res.body()));
            }

            // JSON 형태가 아니면(HTML 등) 바로 에러로 노출
            String ct = res.headers().firstValue("Content-Type").orElse("");
            if (!ct.toLowerCase().contains("json") && !looksLikeJson(res.body())) {
                throw new RuntimeException("Non-JSON response: " + uri + "\n" + head(res.body()));
            }

            return objectMapper.readTree(res.body());
        } catch (Exception e) {
            throw new RuntimeException("TourAPI 호출 실패: " + uri, e);
        }
    }

    private JsonNode firstItem(JsonNode root) {
        if (root == null) return null;
        JsonNode items = root.path("response").path("body").path("items").path("item");
        if (items.isArray() && items.size() > 0) return items.get(0);
        return null;
    }

    private String text(JsonNode node, String field) {
        if (node == null) return null;
        String v = node.path(field).asText(null);
        return (v != null && !v.isBlank()) ? v : null;
    }

    private Double safeDouble(String v) {
        try { return v == null ? null : Double.valueOf(v); }
        catch (Exception e) { return null; }
    }

    private String head(String s) {
        if (s == null) return "";
        return s.substring(0, Math.min(500, s.length())); // 바디 앞부분만
    }

    private boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.startsWith("{") || t.startsWith("[");
    }

}
