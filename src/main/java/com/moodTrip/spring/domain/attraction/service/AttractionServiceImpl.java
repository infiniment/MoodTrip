package com.moodTrip.spring.domain.attraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionRegionResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.AttractionIntro;
import com.moodTrip.spring.domain.attraction.repository.AttractionIntroRepository;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.repository.AttractionEmotionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository repository;
    private final AttractionIntroRepository introRepository;
    private final AttractionEmotionRepository attractionEmotionRepository;
    private final RestTemplate restTemplate;



    @Value("${attraction.apikey.decoding}")
    private String apiKey;

    private final ObjectMapper om = new ObjectMapper();

    private static final String BASE = "https://apis.data.go.kr/B551011/KorWithService2";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ===== 특정 타입 제외 동기화 =====
    @Override
    public int syncAreaBasedListExcluding(int areaCode, Integer sigunguCode, Integer contentTypeId,
                                          int pageSize, long pauseMillis, Set<Integer> excludes) {
        int created = 0, pageNo = 1, total = Integer.MAX_VALUE;
        final Set<Integer> excludeSet = (excludes == null) ? Collections.emptySet() : excludes;

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
                for (JsonNode it : items) {
                    Integer typeId = asInt(it, "contenttypeid");
                    if (typeId != null && excludeSet.contains(typeId)) continue;
                    created += upsertAttraction(it);
                }
            } else if (!items.isMissingNode() && !items.isNull()) {
                Integer typeId = asInt(items, "contenttypeid");
                if (typeId == null || !excludeSet.contains(typeId)) {
                    created += upsertAttraction(items);
                }
            }

            pageNo++;
            sleep(pauseMillis);
        }
        return created;
    }


    // ===== 목록(areaBasedList2) =====
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
        boolean isNew = (a.getAttractionId() == null);

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
        a.setCat1(asText(it, "cat1"));
        a.setCat2(asText(it, "cat2"));
        a.setCat3(asText(it, "cat3"));
        a.setCreatedTime(parseTs(asText(it, "createdtime")));
        a.setModifiedTime(parseTs(asText(it, "modifiedtime")));

        repository.save(a);
        if (isNew || !introRepository.existsById(a.getContentId())) {
            try {
                syncDetailIntro(a.getContentId(), a.getContentTypeId());
            } catch (Exception e) {
                log.warn("intro sync on upsert failed. contentId={}, msg={}", a.getContentId(), e.getMessage());
            }
        }
        return isNew ? 1 : 0;
    }

    // ===== 소개(detailIntro2) =====
    @Override
    public int syncDetailIntro(long contentId, Integer contentTypeId) {
        Integer ctid = (contentTypeId != null)
                ? contentTypeId
                : repository.findByContentId(contentId)
                .map(Attraction::getContentTypeId)
                .orElse(null);

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
        if (!header.hasNonNull("resultCode")) {
            throw new IllegalStateException("detailIntro2 응답 포맷 예외. preview=" + preview);
        }
        String resultCode = header.path("resultCode").asText("");
        if (!"0000".equals(resultCode)) {
            String msg = header.path("resultMsg").asText("");
            throw new IllegalStateException("detailIntro2 오류: " + resultCode + " / " + msg);
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
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(BASE + "/detailIntro2")
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

        try { intro.setRawJson(om.writeValueAsString(it)); }
        catch (JsonProcessingException e) { intro.setRawJson(it.toString()); }

        intro.setSyncedAt(LocalDateTime.now());
        introRepository.save(intro);
    }

    // ===== 조회 =====
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

    // ===== 필터링 검색 (신규) =====
    @Override
    @Transactional(readOnly = true)
    public AttractionRegionResponse findAttractionsFiltered(
            List<String> regionCodes, Pageable pageable,
            String keyword, String cat1, String cat2, String cat3, String sort
    ) {
        List<Integer> areas = (regionCodes == null ? Collections.<Integer>emptyList() :
                regionCodes.stream()
                        .map(RegionCodeMapper::krToAreaCode)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()));

        Pageable sorted = ("name".equalsIgnoreCase(sort) || "portfolio".equalsIgnoreCase(sort))
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("title").ascending())
                : pageable;

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String c1 = (cat1 != null && !cat1.isBlank()) ? cat1.trim() : null;
        String c2 = (cat2 != null && !cat2.isBlank()) ? cat2.trim() : null;
        String c3 = (cat3 != null && !cat3.isBlank()) ? cat3.trim() : null;

        Page<Attraction> page = repository.searchByFilters(
                areas, areas.isEmpty(), kw, c1, c2, c3, sorted
        );
        return AttractionRegionResponse.of(page);
    }

    // ===== 지역별 페이지 응답 =====
    @Override
    @Transactional(readOnly = true)
    public AttractionRegionResponse getRegionAttractions(Integer areaCode, Integer sigunguCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<Attraction> result = (sigunguCode == null)
                ? repository.findByAreaCode(areaCode, pageable)
                : repository.findByAreaCodeAndSigunguCode(areaCode, sigunguCode, pageable);
        return AttractionRegionResponse.of(result);
    }

    // ===== 다중 지역 + 페이징 =====
    @Override
    @Transactional(readOnly = true)
    public AttractionRegionResponse findAttractions(List<Integer> areaCodes, Pageable pageable) {
        if (areaCodes == null || areaCodes.isEmpty()) {
            return AttractionRegionResponse.of(Page.empty(pageable));
        }
        Page<Attraction> page = repository.findByAreaCodeIn(areaCodes, pageable);
        return AttractionRegionResponse.of(page);
    }

    // ===== 상세 정보(단건) 조회 =====
    @Override
    public Optional<AttractionResponse> getDetail(long contentId) {
        return repository.findByContentId(contentId)
                .map(AttractionResponse::from);
    }

    // ===== 전체 페이징 조회 =====
    @Override
    @Transactional(readOnly = true)
    public Page<Attraction> findAttractions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "attractionId"));
        return repository.findAll(pageable);
    }

    // ===== 지역코드(KR**) 목록 조회 + 정렬(옵션) =====
    @Override
    @Transactional(readOnly = true)
    public List<AttractionResponse> findByRegionCodes(List<String> regionCodes, String sort) {
        if (regionCodes == null || regionCodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> areaCodes = regionCodes.stream()
                .map(RegionCodeMapper::krToAreaCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (areaCodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Attraction> list = repository.findByAreaCodeIn(areaCodes);

        String s = (sort == null) ? "default" : sort.trim().toLowerCase(Locale.ROOT);
        if ("name".equals(s) || "portfolio".equals(s)) {
            list = list.stream()
                    .sorted(Comparator.comparing(Attraction::getTitle, java.text.Collator.getInstance(java.util.Locale.KOREAN)))
                    .collect(Collectors.toList());
        }

        return list.stream().map(AttractionResponse::from).collect(Collectors.toList());
    }

    // ===== 감정 태그 기반 카드 조회 =====
    @Override
    @Transactional(readOnly = true)
    public List<AttractionCardDTO> findAttractionsByEmotionIds(List<Integer> emotionIds) {
        var attractions = repository.findAttractionsByEmotionIds(emotionIds);
        return attractions.stream()
                .map(a -> AttractionCardDTO.builder()
                        .attractionId(a.getAttractionId())
                        .title(a.getTitle())
                        .addr1(a.getAddr1())
                        .firstImage(a.getFirstImage())
                        .build())
                .collect(Collectors.toList());
    }

    // ===== 초기 로딩 카드 조회 =====
    @Override
    @Transactional(readOnly = true)
    public List<AttractionCardDTO> findInitialAttractions(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Attraction> attractions = repository.findAll(pageable).getContent();

        return attractions.stream()
                .map(a -> AttractionCardDTO.builder()
                        .attractionId(a.getAttractionId())
                        .title(a.getTitle())
                        .addr1(a.getAddr1())
                        .firstImage(a.getFirstImage())
                        .build())
                .collect(Collectors.toList());
    }

    // ===== 전체 조회 =====
    @Override
    @Transactional(readOnly = true)
    public List<Attraction> getAllAttractions() {
        return repository.findAll();
    }

    // ===== 키워드 검색 =====
    @Override
    @Transactional(readOnly = true)
    public Page<Attraction> searchAttractions(String keyword, int page, int size) {
        return repository.findByTitleContainingIgnoreCase(keyword, PageRequest.of(page, size));
    }


    // ===== Emotion 태그 ID로 관광지 검색 =====
    @Override
    public List<AttractionCardDTO> findAttractionsByEmotionTag(Integer tagId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Attraction> attractionsPage = repository.findByEmotionTagId(tagId, pageable);
        List<Attraction> attractions = attractionsPage.getContent();

        return attractions.stream()
                .map(attraction -> AttractionCardDTO.builder()
                        .attractionId(attraction.getAttractionId())
                        .title(attraction.getTitle())
                        .addr1(attraction.getAddr1())
                        .firstImage(attraction.getFirstImage())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AttractionResponse create(AttractionInsertRequest req) {
        var contentId = req.getContentId();
        var entity = (contentId != null)
                ? repository.findByContentId(contentId).orElseGet(req::toEntity)
                : req.toEntity();
        var saved = repository.save(entity);
        try {
            if (!introRepository.existsById(saved.getContentId())) {
                syncDetailIntro(saved.getContentId(), saved.getContentTypeId());
            }
        } catch (Exception e) {
            log.warn("intro sync on create failed. contentId={}, msg={}", saved.getContentId(), e.getMessage());
        }

        return AttractionResponse.from(saved);
    }

    // ===== 소개 정보 조회 (없으면 API 호출 후 저장) =====
    @Override
    @Transactional
    public AttractionIntro getIntro(long contentId, Integer contentTypeId) {
        var intro = introRepository.findById(contentId).orElse(null); // PK = contentId
        if (intro == null) {
            try {
                syncDetailIntro(contentId, contentTypeId);           // TourAPI 호출 + 업서트
                intro = introRepository.findById(contentId).orElse(null);
            } catch (Exception e) {
                // 실패해도 화면은 떠야 하므로 조용히 폴백
                log.warn("detailIntro2 sync failed. contentId={}, msg={}", contentId, e.getMessage());
            }
        }
        return intro; // null 이어도 아래 normalizeIntro가 안전 폴백함
    }

    // ===== 상세 정보 응답 생성 (기본정보 + 소개정보) =====
    @Override
    @Transactional(readOnly = true)
    public AttractionDetailResponse getDetailResponse(long contentId) {
        var base = getDetail(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Attraction not found: " + contentId));

        var intro = getIntro(contentId, base.getContentTypeId());       // 없으면 동기화 시도
        var introNorm = normalizeIntro(intro);                          // TODO: 분기 필드 없앴다면 지워도 됨

        AttractionDetailResponse.DetailCommon common;
        try {
            common = fetchDetailCommon(contentId, base.getContentTypeId());
        } catch (Exception e) {
            log.warn("detailCommon2 fetch failed. contentId={}, msg={}", contentId, e.getMessage());
            common = AttractionDetailResponse.DetailCommon.builder().build();
        }

        return AttractionDetailResponse.of(base, introNorm, common);
    }
    // ===== intro 정규화 =====
    private AttractionDetailResponse.IntroNormalized normalizeIntro(AttractionIntro i) {
        if (i == null) return AttractionDetailResponse.IntroNormalized.builder().build();

        // ✅ 예전에는 분기별 필드(firstNonEmpty) 사용 → 현재는 단일 필드만 남겨둔 상태
        return AttractionDetailResponse.IntroNormalized.builder()
                .infocenter(i.getInfocenter())
                .usetime(i.getUsetime())
                .restdate(i.getRestdate())
                .parking(i.getParking())
                .age(i.getExpagerange() != null ? i.getExpagerange() : i.getAgelimit())
                .build();
    }



    // ===== detailCommon 호출 + overview 저장 =====
    private AttractionDetailResponse.DetailCommon fetchDetailCommon(long contentId, Integer contentTypeId) {
        var uri = UriComponentsBuilder.fromUriString(BASE + "/detailCommon2")
                .queryParam("serviceKey", apiKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build(apiKey != null && apiKey.contains("%"))
                .toUri();

        log.info("TourAPI GET {}", uri.toString().replaceAll("serviceKey=[^&]+", "serviceKey=***"));

        String raw = restTemplate.getForObject(uri, String.class);
        String trimmed = raw == null ? "" : raw.trim();
        if (!trimmed.isEmpty() && trimmed.charAt(0) == '<') {
            throw new IllegalStateException("detailCommon2가 XML 에러를 반환. preview=" +
                    (raw == null ? "null" : raw.substring(0, Math.min(raw.length(), 400))));
        }

        JsonNode root = safe(parseJson(raw));
        JsonNode item = root.path("response").path("body").path("items").path("item");
        if (item.isArray()) item = item.size() > 0 ? item.get(0) : om.createObjectNode();

        String tel = firstNonEmpty(asText(item, "tel"), asText(item, "telname"));
        String overview = asText(item, "overview");
        String addr1 = asText(item, "addr1");
        String addr2 = asText(item, "addr2");

        // 👇 DB에 overview 업데이트 추가
        introRepository.findById(contentId).ifPresent(intro -> {
            intro.setOverview(overview);
            intro.setSyncedAt(LocalDateTime.now());
            introRepository.save(intro);
        });

        return AttractionDetailResponse.DetailCommon.builder()
                .tel(tel)
                .overview(overview)
                .addrDisplay(joinNonBlankSpace(addr1, addr2))
                .build();
    }

    // 공백/널 제거하며 주소 결합
    private String joinNonBlankSpace(String... parts) {
        if (parts == null) return null;
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(p);
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }
    // ===== 지역 코드 매퍼 =====
    static final class RegionCodeMapper {
        private static final Map<String, Integer> KR_TO_AREA = new HashMap<>();
        private static final Map<Integer, String> AREA_TO_NAME = new HashMap<>();
        static {
            KR_TO_AREA.put("KR11", 1);  AREA_TO_NAME.put(1,  "서울");
            KR_TO_AREA.put("KR28", 2);  AREA_TO_NAME.put(2,  "인천");
            KR_TO_AREA.put("KR30", 3);  AREA_TO_NAME.put(3,  "대전");
            KR_TO_AREA.put("KR27", 4);  AREA_TO_NAME.put(4,  "대구");
            KR_TO_AREA.put("KR29", 5);  AREA_TO_NAME.put(5,  "광주");
            KR_TO_AREA.put("KR26", 6);  AREA_TO_NAME.put(6,  "부산");
            KR_TO_AREA.put("KR31", 7);  AREA_TO_NAME.put(7,  "울산");
            KR_TO_AREA.put("KR50", 8);  AREA_TO_NAME.put(8,  "세종");
            KR_TO_AREA.put("KR41", 31); AREA_TO_NAME.put(31, "경기");
            KR_TO_AREA.put("KR42", 32); AREA_TO_NAME.put(32, "강원");
            KR_TO_AREA.put("KR43", 33); AREA_TO_NAME.put(33, "충북");
            KR_TO_AREA.put("KR44", 34); AREA_TO_NAME.put(34, "충남");
            KR_TO_AREA.put("KR47", 35); AREA_TO_NAME.put(35, "경북");
            KR_TO_AREA.put("KR48", 36); AREA_TO_NAME.put(36, "경남");
            KR_TO_AREA.put("KR45", 37); AREA_TO_NAME.put(37, "전북");
            KR_TO_AREA.put("KR46", 38); AREA_TO_NAME.put(38, "전남");
            KR_TO_AREA.put("KR49", 39); AREA_TO_NAME.put(39, "제주");
        }
        static Integer krToAreaCode(String kr) { return KR_TO_AREA.get(kr); }
        static String areaCodeToName(Integer area) { return AREA_TO_NAME.get(area); }
        private RegionCodeMapper() {}
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
    private LocalDateTime parseTs(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDateTime.parse(s, TS); } catch (Exception e) { return null; }
    }
    private String firstNonEmpty(String... arr) {
        for (String s : arr) if (s != null && !s.isBlank()) return s;
        return null;
    }
    private void sleep(long ms) {
        if (ms <= 0) return;
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    @PostConstruct
    void checkApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("attraction.apikey.decoding 비어있음 (application-local.yml 확인)");
        }
        apiKey = apiKey.trim();
        log.info("TourAPI key loaded. len={}, tail={}", apiKey.length(),
                apiKey.length() > 4 ? apiKey.substring(apiKey.length() - 4) : "****");
    }

    @Override
    public List<String> getEmotionTagNames(long contentId) {
        // Top 3만 원하면 .stream().limit(3) 추가
        return attractionEmotionRepository.findActiveEmotionNamesByContentId(contentId);
    }

    @Override
    public Attraction getEntityByContentId(Long contentId) {
        return repository.findByContentId(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Attraction not found by contentId=" + contentId));
    }
}
