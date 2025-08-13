package com.moodTrip.spring.domain.attraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.AttractionIntro;
import com.moodTrip.spring.domain.attraction.repository.AttractionIntroRepository;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository repository;
    private final AttractionIntroRepository introRepository;
    private final RestTemplate restTemplate;

    @Value("${attraction.apikey.decoding}")
    private String apiKey;

    private final ObjectMapper om = new ObjectMapper();

    private static final String BASE = "https://apis.data.go.kr/B551011/KorWithService2";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ===== 동기화(areaBasedList2) =====
    @Override
    public int syncAreaBasedList(int areaCode, Integer sigunguCode, Integer contentTypeId,
                                 int pageSize, long pauseMillis) {
        int created = 0, pageNo = 1, total = Integer.MAX_VALUE;

        while ((pageNo - 1) * pageSize < total) {
            URI uri = buildAreaBasedListUri(areaCode, sigunguCode, contentTypeId, pageSize, pageNo);
            log.info("TourAPI GET {}", uri.toString().replaceAll("serviceKey=[^&]+", "serviceKey=***"));

            String body = restTemplate.getForObject(uri, String.class);
            String preview = body == null ? "null" : body.substring(0, Math.min(body.length(), 300));
            log.info("areaBasedList2 preview: {}", preview);

            String trimmed = body == null ? "" : body.trim();
            if (!trimmed.isEmpty() && trimmed.charAt(0) == '<') {
                throw new IllegalStateException("TourAPI가 JSON 대신 XML 에러를 반환. preview=" + preview);
            }

            JsonNode root = safe(parseJson(body));
            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText("");
            if (!"0000".equals(resultCode)) {
                String msg = header.path("resultMsg").asText("");
                throw new IllegalStateException("TourAPI 오류: " + resultCode + " / " + msg);
            }

            JsonNode bodyNode = root.path("response").path("body");
            total = bodyNode.path("totalCount").asInt(0);
            JsonNode items = bodyNode.path("items").path("item");

            if (items.isArray()) {
                for (JsonNode it : items) created += upsertAttraction(it);
            } else if (!items.isMissingNode() && !items.isNull()) {
                created += upsertAttraction(items);
            }

            pageNo++;
            sleep(pauseMillis);
        }
        return created;
    }

    private URI buildAreaBasedListUri(int areaCode, Integer sigunguCode, Integer contentTypeId,
                                      int pageSize, int pageNo) {
        boolean alreadyEncoded = apiKey != null && apiKey.contains("%");
        return UriComponentsBuilder.fromUriString(BASE + "/areaBasedList2")
                .queryParam("serviceKey", apiKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("areaCode", areaCode)
                .queryParam("numOfRows", pageSize)
                .queryParam("pageNo", pageNo)
                .queryParam("arrange", "A")
                .queryParamIfPresent("sigunguCode", Optional.ofNullable(sigunguCode))
                .queryParamIfPresent("contentTypeId", Optional.ofNullable(contentTypeId))
                .build(alreadyEncoded)
                .toUri();
    }

    private int upsertAttraction(JsonNode it) {
        long contentId = asLong(it, "contentid");
        if (contentId == 0L) return 0;

        Attraction a = repository.findByContentId(contentId)
                .orElseGet(() -> Attraction.builder().contentId(contentId).build());
        boolean isNew = (a.getId() == null);

        a.setContentTypeId(asInt(it, "contenttypeid"));
        a.setTitle(asText(it, "title"));
        a.setAddr1(asText(it, "addr1"));
        a.setAddr2(asText(it, "addr2"));
        a.setZipcode(asText(it, "zipcode"));
        a.setTel(asText(it, "tel"));
        a.setFirstImage(asText(it, "firstimage"));
        a.setFirstImage2(asText(it, "firstimage2"));
        a.setMapX(asDouble(it, "mapx"));
        a.setMapY(asDouble(it, "mapy"));
        a.setMlevel(asInt(it, "mlevel"));
        a.setAreaCode(asInt(it, "areacode"));
        a.setSigunguCode(asInt(it, "sigungucode"));
        a.setCreatedTime(parseTs(asText(it, "createdtime")));
        a.setModifiedTime(parseTs(asText(it, "modifiedtime")));

        repository.save(a);
        return isNew ? 1 : 0;
    }

    // ===== 소개(detailIntro2) =====
    @Override
    public int syncDetailIntro(long contentId, Integer contentTypeId) {
        Integer ctid = (contentTypeId != null)
                ? contentTypeId
                : repository.findByContentId(contentId).map(Attraction::getContentTypeId).orElse(null);

        URI uri = buildDetailIntroUri(contentId, ctid);
        log.info("TourAPI GET {}", uri.toString().replaceAll("serviceKey=[^&]+", "serviceKey=***"));

        String body = restTemplate.getForObject(uri, String.class);
        String preview = body == null ? "null" : body.substring(0, Math.min(body.length(), 400));
        log.info("detailIntro2 preview: {}", preview);

        String trimmed = body == null ? "" : body.trim();
        if (!trimmed.isEmpty() && trimmed.charAt(0) == '<') {
            throw new IllegalStateException("detailIntro2가 XML 에러를 반환. preview=" + preview);
        }

        JsonNode root = safe(parseJson(body));
        JsonNode header = root.path("response").path("header");
        if (!"0000".equals(header.path("resultCode").asText(""))) {
            String msg = header.path("resultMsg").asText("");
            throw new IllegalStateException("detailIntro2 오류: " + msg);
        }

        JsonNode item = root.path("response").path("body").path("items").path("item");
        if (item.isArray()) {
            if (item.size() == 0) return 0;
            item = item.get(0);
        }
        if (item.isMissingNode() || item.isNull()) return 0;

        upsertIntro(item);
        return 1;
    }

    @Override
    public int syncDetailIntroByArea(int areaCode, Integer sigunguCode, Integer contentTypeId, long pauseMillis) {
        List<Attraction> targets = (sigunguCode == null)
                ? repository.findAllByAreaCode(areaCode)
                : repository.findAllByAreaCodeAndSigunguCode(areaCode, sigunguCode);
        if (contentTypeId != null) {
            targets.removeIf(a -> !contentTypeId.equals(a.getContentTypeId()));
        }

        int saved = 0;
        for (Attraction a : targets) {
            try {
                saved += syncDetailIntro(a.getContentId(), a.getContentTypeId());
            } catch (Exception e) {
                log.warn("intro sync fail contentId={} : {}", a.getContentId(), e.getMessage());
            }
            sleep(pauseMillis);
        }
        return saved;
    }

    private URI buildDetailIntroUri(long contentId, Integer contentTypeId) {
        boolean alreadyEncoded = apiKey != null && apiKey.contains("%");
        var b = UriComponentsBuilder.fromUriString(BASE + "/detailIntro2")
                .queryParam("serviceKey", apiKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId);
        if (contentTypeId != null) b.queryParam("contentTypeId", contentTypeId);
        return b.build(alreadyEncoded).toUri();
    }

    private void upsertIntro(JsonNode it) {
        long contentId = asLong(it, "contentid");
        if (contentId == 0L) return;

        Integer ctype = asInt(it, "contenttypeid");
        AttractionIntro intro = introRepository.findById(contentId)
                .orElse(AttractionIntro.builder().contentId(contentId).build());

        intro.setContentTypeId(ctype);
        intro.setInfocenter(firstNonEmpty(
                asText(it,"infocenter"), asText(it,"infocenterlodging"),
                asText(it,"infocenterfood"), asText(it,"infocenterculture"),
                asText(it,"infocentershopping"), asText(it,"infocenterleports"),
                asText(it,"infocentertourcourse")
        ));
        intro.setUsetime(firstNonEmpty(
                asText(it,"usetime"), asText(it,"usetimeculture"),
                asText(it,"usetimefestival"), asText(it,"usetimeleports"),
                asText(it,"opentime"), asText(it,"opentimefood")
        ));
        intro.setUsefee(firstNonEmpty(asText(it,"usefee"), asText(it,"usefeeleports")));
        intro.setParking(firstNonEmpty(
                asText(it,"parking"), asText(it,"parkingfood"),
                asText(it,"parkingculture"), asText(it,"parkingshopping"),
                asText(it,"parkinglodging"), asText(it,"parkingleports")
        ));
        intro.setRestdate(firstNonEmpty(
                asText(it,"restdate"), asText(it,"restdatefood"),
                asText(it,"restdateculture"), asText(it,"restdateshopping"),
                asText(it,"restdateleports")
        ));
        intro.setChkcreditcard(firstNonEmpty(
                asText(it,"chkcreditcard"), asText(it,"chkcreditcardfood"),
                asText(it,"chkcreditcardculture"), asText(it,"chkcreditcardshopping"),
                asText(it,"chkcreditcardleports")
        ));
        intro.setChkbabycarriage(firstNonEmpty(
                asText(it,"chkbabycarriage"), asText(it,"chkbabycarriageshopping"),
                asText(it,"chkbabycarriageleports"), asText(it,"chkbabycarriageculture")
        ));
        intro.setChkpet(firstNonEmpty(
                asText(it,"chkpet"), asText(it,"chkpetculture"),
                asText(it,"chkpetshopping"), asText(it,"chkpetleports")
        ));

        try { intro.setRawJson(om.writeValueAsString(it)); }
        catch (JsonProcessingException e) { intro.setRawJson(it.toString()); }

        intro.setSyncedAt(LocalDateTime.now());
        introRepository.save(intro);
    }

    // ===== 조회(단순 필터 리스트용) =====
    @Transactional(readOnly = true)
    @Override
    public List<Attraction> find(int areaCode, Integer sigunguCode, Integer contentTypeId) {
        if (sigunguCode == null && contentTypeId == null) {
            return repository.findAllByAreaCode(areaCode);
        }
        if (sigunguCode == null) {
            return repository.findAllByAreaCodeAndContentTypeId(areaCode, contentTypeId);
        }
        if (contentTypeId == null) {
            return repository.findAllByAreaCodeAndSigunguCode(areaCode, sigunguCode);
        }
        return repository.findAllByAreaCodeAndSigunguCodeAndContentTypeId(areaCode, sigunguCode, contentTypeId);
    }

    // ===== 통합 검색 (키워드+필터, 제목 앞글자 우선, 페이지네이션) =====
    @Transactional(readOnly = true)
    @Override
    public Page<Attraction> searchKeywordPrefTitleStarts(String q, Integer area, Integer si, Integer type, int page, int size) {
        return repository.searchKeywordPrefTitleStarts(q, area, si, type, PageRequest.of(page, size));
    }

    // ===== 수동 등록 =====
    @Override
    public AttractionResponse create(AttractionInsertRequest req) {
        var contentId = req.getContentId();
        var entity = (contentId != null)
                ? repository.findByContentId(contentId).orElseGet(req::toEntity)
                : req.toEntity();
        var saved = repository.save(entity);
        return AttractionResponse.from(saved);
    }

    // ===== 공통 유틸 =====
    private JsonNode parseJson(String body) {
        try { return om.readTree(body == null ? "{}" : body); }
        catch (JsonProcessingException e) {
            String preview = body == null ? "null" : body.substring(0, Math.min(body.length(), 500));
            throw new IllegalStateException("JSON 파싱 실패: " + e.getOriginalMessage() + " / preview=" + preview, e);
        }
    }
    private JsonNode safe(JsonNode n) { return n == null ? om.createObjectNode() : n; }
    private String asText(JsonNode n, String k) {
        JsonNode v = n.path(k);
        if (v.isMissingNode() || v.isNull()) return null;
        String s = v.asText();
        return (s == null || s.isBlank()) ? null : s;
    }
    private Long asLong(JsonNode n, String k) {
        String s = asText(n, k);
        if (s == null) return 0L;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return 0L; }
    }
    private Integer asInt(JsonNode n, String k) {
        String s = asText(n, k);
        if (s == null) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }
    private Double asDouble(JsonNode n, String k) {
        String s = asText(n, k);
        if (s == null) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }
    private java.time.LocalDateTime parseTs(String s) {
        if (s == null || s.isBlank()) return null;
        try { return java.time.LocalDateTime.parse(s, TS); } catch (Exception e) { return null; }
    }
    private String firstNonEmpty(String... arr) { for (String s : arr) if (s != null && !s.isBlank()) return s; return null; }
    private void sleep(long ms) { if (ms <= 0) return; try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); } }

    @PostConstruct
    void checkApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("attraction.apikey.decoding 비어있음 (application-local.yml 확인)");
        }
        apiKey = apiKey.trim();
        log.info("TourAPI key loaded. len={}, tail={}", apiKey.length(),
                apiKey.length() > 4 ? apiKey.substring(apiKey.length() - 4) : "****");
    }
}
