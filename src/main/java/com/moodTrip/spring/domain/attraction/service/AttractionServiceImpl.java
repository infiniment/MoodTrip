package com.moodTrip.spring.domain.attraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.admin.dto.response.AttractionAdminDto;
import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionRegionResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.AttractionIntro;
import com.moodTrip.spring.domain.attraction.repository.AttractionIntroRepository;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.attraction.repository.UserAttractionRepository;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.repository.AttractionEmotionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
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
    private final UserAttractionRepository userAttractionRepository;


    @Value("${attraction.apikey.decoding}")
    private String apiKey;

    @Value("${attraction.apikey.encoding}")
    private String encodeApiKey;

    private final ObjectMapper om = new ObjectMapper();

    private static final String BASE = "https://apis.data.go.kr/B551011/KorWithService2";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // 12(관광지), 14(문화시설)만 동기화
    @Override
    public int syncAreaBasedListOnly12And14(int areaCode, Integer sigunguCode,
                                            int pageSize, long pauseMillis) {
        int created = 0;
        created += syncAreaBasedList(areaCode, sigunguCode, 12, pageSize, pauseMillis);
        created += syncAreaBasedList(areaCode, sigunguCode, 14, pageSize, pauseMillis);
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
        return UriComponentsBuilder.fromUriString(BASE + "/areaBasedList2")
                .queryParam("serviceKey", apiKey) // ✅ decoding key
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("areaCode", areaCode)
                .queryParam("numOfRows", pageSize)
                .queryParam("pageNo", pageNo)
                .queryParam("arrange", "A")
                .queryParamIfPresent("sigunguCode", Optional.ofNullable(sigunguCode))
                .queryParamIfPresent("contentTypeId", Optional.ofNullable(contentTypeId))
                .build(false) // ✅ 항상 false
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
        if (body != null && body.trim().startsWith("<")) {
            log.error("TourAPI returned XML instead of JSON: {}", body.substring(0, 200));
            throw new IllegalStateException("TourAPI returned XML. ServiceKey may be wrong.");
        }
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
    @Override
    @Transactional
    public AttractionDetailResponse getDetailResponse(Long contentId) {
        Integer typeId = repository.findByContentId(contentId)
                .map(Attraction::getContentTypeId)
                .orElse(null);

        return getDetailResponse(contentId, typeId);
    }

    @Override
    @Transactional
    public AttractionDetailResponse getDetailResponse(Long contentId, Integer contentTypeId) {
        Attraction base = repository.findByContentId(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Attraction not found: " + contentId));

        Integer finalTypeId = (contentTypeId != null) ? contentTypeId : base.getContentTypeId();

        // intro / a11y / overview 각각 독립 호출
        try { syncDetailIntro(contentId, finalTypeId); }
        catch (Exception e) { log.warn("intro sync fail contentId={} : {}", contentId, e.getMessage()); }

        try { syncDetailWithTour(contentId, finalTypeId); }
        catch (Exception e) { log.warn("a11y sync fail contentId={} : {}", contentId, e.getMessage()); }

        try { syncOverview(contentId, finalTypeId); }
        catch (Exception e) { log.warn("overview sync fail contentId={} : {}", contentId, e.getMessage()); }

        // 최신 intro 조회
        AttractionIntro intro = introRepository.findById(contentId).orElse(null);
        AttractionDetailResponse.IntroNormalized introNorm = normalizeIntro(intro != null ? intro : new AttractionIntro());
        AttractionResponse baseResp = AttractionResponse.from(base);

        AttractionDetailResponse.DetailCommon common =
                (intro != null)
                        ? AttractionDetailResponse.DetailCommon.builder()
                        .overview(intro.getOverview())
                        .infocenter(intro.getInfocenter())
                        .usetime(intro.getUsetime())
                        .restdate(intro.getRestdate())
                        .parking(intro.getParking())
                        .build()
                        : AttractionDetailResponse.DetailCommon.builder().build();

        return AttractionDetailResponse.of(baseResp, introNorm, common, intro);
    }

    private void syncDetailWithTour(long contentId, Integer contentTypeId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE + "/detailWithTour2")
                .queryParam("serviceKey", apiKey) // decoding key
                .queryParam("_type", "json")
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build(false)
                .toUri();

        String body = restTemplate.getForObject(uri, String.class);
        JsonNode item = extractItem(body);

        if (item.isMissingNode()) return;

        AttractionIntro intro = introRepository.findById(contentId)
                .orElseGet(() -> AttractionIntro.builder()
                        .contentId(contentId)
                        .contentTypeId(contentTypeId)
                        .build());

        setIfHasText(intro::setWheelchair, asText(item, "wheelchair"));
        setIfHasText(intro::setElevator, asText(item, "elevator"));
        setIfHasText(intro::setBraileblock, asText(item, "braileblock"));
        setIfHasText(intro::setExit, asText(item, "exit"));
        setIfHasText(intro::setGuidesystem, asText(item, "guidesystem"));
        setIfHasText(intro::setSignguide, asText(item, "signguide"));
        setIfHasText(intro::setVideoguide, asText(item, "videoguide"));
        setIfHasText(intro::setAudioguide, asText(item, "audioguide"));
        setIfHasText(intro::setBigprint, asText(item, "bigprint"));
        setIfHasText(intro::setBrailepromotion, asText(item, "brailepromotion"));
        setIfHasText(intro::setHelpdog, asText(item, "helpdog"));
        setIfHasText(intro::setInfantsfamilyetc, asText(item, "infantsfamilyetc"));
        setIfHasText(intro::setHearingroom, asText(item, "hearingroom"));
        setIfHasText(intro::setHearinghandicapetc, asText(item, "hearinghandicapetc"));
        setIfHasText(intro::setBlindhandicapetc, asText(item, "blindhandicapetc"));
        setIfHasText(intro::setHandicapetc, asText(item, "handicapetc"));
        setIfHasText(intro::setRestroom, asText(item, "restroom"));
        setIfHasText(intro::setPublictransport, asText(item, "publictransport"));

        intro.setSyncedAt(LocalDateTime.now());
        introRepository.save(intro);
    }

    private void syncOverview(long contentId, Integer contentTypeId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE + "/detailCommon2")
                .queryParam("serviceKey", encodeApiKey) // ✅ encoding key
                .queryParam("_type", "json")
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("overviewYN", "Y")
                .build(true)
                .toUri();

        String body = restTemplate.getForObject(uri, String.class);
        JsonNode item = extractItem(body);

        String overview = asText(item, "overview");
        if (StringUtils.hasText(overview)) {
            AttractionIntro intro = introRepository.findById(contentId)
                    .orElseGet(() -> AttractionIntro.builder()
                            .contentId(contentId)
                            .contentTypeId(contentTypeId)
                            .build());
            intro.setOverview(overview);
            intro.setSyncedAt(LocalDateTime.now());
            introRepository.save(intro);
        }
    }

    private URI buildDetailIntroUri(long contentId, Integer contentTypeId) {
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(BASE + "/detailIntro2")
                .queryParam("serviceKey", apiKey) // ✅ decoding key
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId);
        if (contentTypeId != null) b.queryParam("contentTypeId", contentTypeId);
        return b.build(false) // ✅ 항상 false
                .toUri();
    }

    private void upsertIntro(JsonNode it) {
        long contentId = asLong(it, "contentid");
        if (contentId == 0L) return;

        Integer ctype = asInt(it, "contenttypeid");
        AttractionIntro intro = introRepository.findById(contentId)
                .orElse(AttractionIntro.builder().contentId(contentId).build());

        intro.setContentTypeId(ctype);

        // ===== 12/14 공통 필드 =====
        intro.setInfocenter(firstNonEmpty(
                asText(it,"infocenter")
        ));
        intro.setUsetime(firstNonEmpty(
                asText(it,"usetime"),
                asText(it,"usetimeculture")
        ));
        intro.setUsefee(firstNonEmpty(
                asText(it,"usefee")
        ));
        intro.setParking(firstNonEmpty(
                asText(it,"parking"),
                asText(it,"parkingculture")
        ));
        intro.setRestdate(firstNonEmpty(
                asText(it,"restdate"),
                asText(it,"restdateculture")
        ));
        intro.setExpagerange(firstNonEmpty(
                asText(it,"expagerange"),
                asText(it,"agelimit")
        ));

        // ===== raw_json 백업 =====
        try {
            intro.setRawJson(om.writeValueAsString(it));
        } catch (JsonProcessingException e) {
            intro.setRawJson(it.toString());
        }

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
                        .contentId(a.getContentId())
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
        List<Attraction> attractions = repository.findDistinctAttractions(pageable).getContent();

        return attractions.stream()
                .map(a -> AttractionCardDTO.builder()
                        .attractionId(a.getAttractionId())
                        .contentId(a.getContentId())
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
                        .contentId(attraction.getContentId())
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
                .wheelchair(i.getWheelchair())
                .elevator(i.getElevator())
                .braileblock(i.getBraileblock())
                .exit(i.getExit())
                .guidesystem(i.getGuidesystem())
                .signguide(i.getSignguide())
                .videoguide(i.getVideoguide())
                .audioguide(i.getAudioguide())
                .bigprint(i.getBigprint())
                .brailepromotion(i.getBrailepromotion())
                .helpdog(i.getHelpdog())
                .infantsfamilyetc(i.getInfantsfamilyetc())
                .hearingroom(i.getHearingroom())
                .hearinghandicapetc(i.getHearinghandicapetc())
                .blindhandicapetc(i.getBlindhandicapetc())
                .handicapetc(i.getHandicapetc())
                .build();
    }

    private void setIfHasText(java.util.function.Consumer<String> setter, String v) {
        if (StringUtils.hasText(v)) setter.accept(v);
    }
    private JsonNode extractItem(String raw) {
        if (raw == null || raw.isBlank()) return om.createObjectNode();
        String trimmed = raw.trim();

        // XML로 시작하는 경우 fallback 처리
        if (trimmed.startsWith("<")) {
            log.warn("TourAPI returned XML (fallback parse). preview={}",
                    trimmed.substring(0, Math.min(200, trimmed.length())));
            try {
                // 단순히 overview 태그만 추출
                int start = trimmed.indexOf("<overview>");
                int end = trimmed.indexOf("</overview>");
                if (start != -1 && end != -1) {
                    String overview = trimmed.substring(start + 10, end).trim();
                    return om.createObjectNode().put("overview", overview);
                }
            } catch (Exception ignore) {}
            return om.createObjectNode();
        }

        try {
            JsonNode root = om.readTree(raw);
            JsonNode item = root.path("response").path("body").path("items").path("item");
            return item.isArray() ? (item.size() > 0 ? item.get(0) : om.createObjectNode()) : item;
        } catch (Exception e) {
            throw new IllegalStateException("JSON parse failed", e);
        }
    }

    /** overview만 안전하게 가져오는 헬퍼 */
    private String fetchOverviewWithFailover(
            String base, long contentId, Integer contentTypeId, HttpEntity<Void> entity) {

        // ✅ contentTypeId 보강: DB에서 가져오기
        if (contentTypeId == null) {
            contentTypeId = repository.findByContentId(contentId)
                    .map(Attraction::getContentTypeId)
                    .orElse(null);
        }

        if (contentTypeId == null) {
            log.warn("fetchOverview skipped: no contentTypeId in DB (contentId={})", contentId);
            return "{}";
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(base + "/detailCommon2")
                    .queryParam("serviceKey", encodeApiKey) // ← 디코딩 키
                    .queryParam("_type", "json")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "moodTrip")
                    .queryParam("contentId", contentId)
                    .queryParam("contentTypeId", contentTypeId)
                    .queryParam("overviewYN", "Y")
                    .build(true) // ← 다시 인코딩 시켜야 하니까 false
                    .toUri();


            log.info("TourAPI GET overview: {}", uri.toString().replaceAll("serviceKey=[^&]+", "serviceKey=***"));

            String body = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody();
            if (isJsonOkAndHasOverview(body)) return body;

        } catch (Exception e) {
            log.warn("overview fetch failed contentId={} : {}", contentId, e.getMessage());
        }

        return "{}";
    }


    private boolean isJsonOkAndHasOverview(String body) {
        if (body == null) return false;
        String trimmed = body.trim();
        if (!trimmed.isEmpty() && trimmed.charAt(0) == '<') return false; // XML Fault
        try {
            JsonNode root = om.readTree(body);
            String code = root.path("response").path("header").path("resultCode").asText("");
            if (!"0000".equals(code)) return false;
            JsonNode item = root.path("response").path("body").path("items").path("item");
            if (item.isArray()) item = item.size() > 0 ? item.get(0) : om.createObjectNode();
            String overview = asText(item, "overview");
            return org.springframework.util.StringUtils.hasText(overview);
        } catch (Exception ignore) {
            return false;
        }
    }



    private boolean a11yIsEmpty(AttractionIntro i) {
        if (i == null) return true;
        return !(org.springframework.util.StringUtils.hasText(i.getWheelchair())
                || org.springframework.util.StringUtils.hasText(i.getElevator())
                || org.springframework.util.StringUtils.hasText(i.getBraileblock())
                || org.springframework.util.StringUtils.hasText(i.getExit())
                || org.springframework.util.StringUtils.hasText(i.getGuidesystem())
                || org.springframework.util.StringUtils.hasText(i.getSignguide())
                || org.springframework.util.StringUtils.hasText(i.getVideoguide())
                || org.springframework.util.StringUtils.hasText(i.getAudioguide())
                || org.springframework.util.StringUtils.hasText(i.getBigprint())
                || org.springframework.util.StringUtils.hasText(i.getBrailepromotion())
                || org.springframework.util.StringUtils.hasText(i.getHelpdog())
                || org.springframework.util.StringUtils.hasText(i.getInfantsfamilyetc())
                || org.springframework.util.StringUtils.hasText(i.getHearingroom())
                || org.springframework.util.StringUtils.hasText(i.getHearinghandicapetc())
                || org.springframework.util.StringUtils.hasText(i.getBlindhandicapetc())
                || org.springframework.util.StringUtils.hasText(i.getHandicapetc()));
    }

    private URI buildAreaBasedSyncList2Uri(Integer areaCode, Integer sigunguCode, Integer contentTypeId,
                                           int pageSize, int pageNo) {
        return UriComponentsBuilder.fromUriString(BASE + "/areaBasedSyncList2")
                .queryParam("serviceKey", apiKey) // ✅ decoding key
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "moodTrip")
                .queryParam("_type", "json")
                .queryParamIfPresent("areaCode", Optional.ofNullable(areaCode))
                .queryParamIfPresent("sigunguCode", Optional.ofNullable(sigunguCode))
                .queryParamIfPresent("contentTypeId", Optional.ofNullable(contentTypeId))
                .queryParam("numOfRows", pageSize)
                .queryParam("pageNo", pageNo)
                .build(false) // ✅ 항상 false
                .toUri();
    }

    /** detailWithTour2가 비어올 때, 동일 contentId를 /areaBasedSyncList2에서 찾아 a11y만 보강 저장 */
    private int syncA11yFromAreaBasedSyncList2(long contentId) {
        var base = repository.findByContentId(contentId).orElse(null);
        if (base == null) return 0;

        int pageNo = 1, pageSize = 200, total = Integer.MAX_VALUE;
        while ((pageNo - 1) * pageSize < total) {
            URI uri = buildAreaBasedSyncList2Uri(base.getAreaCode(), base.getSigunguCode(), base.getContentTypeId(), pageSize, pageNo);
            String body = restTemplate.getForObject(uri, String.class);
            if (body != null && body.trim().startsWith("<")) {
                log.error("TourAPI returned XML instead of JSON: {}", body.substring(0, 200));
                throw new IllegalStateException("TourAPI returned XML. ServiceKey may be wrong.");
            }
            JsonNode root = safe(parseJson(body));
            JsonNode bodyNode = root.path("response").path("body");
            total = bodyNode.path("totalCount").asInt(0);
            JsonNode items = bodyNode.path("items").path("item");

            if (items.isArray()) {
                for (JsonNode it : items) {
                    if (asLong(it, "contentid") == contentId) {
                        AttractionIntro intro = introRepository.findById(contentId)
                                .orElse(AttractionIntro.builder()
                                        .contentId(contentId)
                                        .contentTypeId(base.getContentTypeId())
                                        .build());

                        setIfHasText(intro::setWheelchair,       asText(it, "wheelchair"));
                        setIfHasText(intro::setElevator,         asText(it, "elevator"));
                        setIfHasText(intro::setBraileblock,      asText(it, "braileblock"));
                        setIfHasText(intro::setExit,             asText(it, "exit"));
                        setIfHasText(intro::setGuidesystem,      asText(it, "guidesystem"));
                        setIfHasText(intro::setSignguide,        asText(it, "signguide"));
                        setIfHasText(intro::setVideoguide,       asText(it, "videoguide"));
                        setIfHasText(intro::setAudioguide,       asText(it, "audioguide"));
                        setIfHasText(intro::setBigprint,         asText(it, "bigprint"));
                        setIfHasText(intro::setBrailepromotion,  asText(it, "brailepromotion"));
                        setIfHasText(intro::setHelpdog,          asText(it, "helpdog"));
                        setIfHasText(intro::setInfantsfamilyetc, asText(it, "infantsfamilyetc"));
                        setIfHasText(intro::setHearingroom,      asText(it, "hearingroom"));
                        setIfHasText(intro::setHearinghandicapetc, asText(it, "hearinghandicapetc"));
                        setIfHasText(intro::setBlindhandicapetc,   asText(it, "blindhandicapetc"));
                        setIfHasText(intro::setHandicapetc,      asText(it, "handicapetc"));

                        intro.setSyncedAt(LocalDateTime.now());
                        introRepository.save(intro);
                        return 1;
                    }
                }
            }
            pageNo++;
        }
        return 0;
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

    @Override
    @Transactional(readOnly = true)
    public Page<AttractionAdminDto> getAttractionsForAdmin(String search, int page, int size) {
        Page<Attraction> attractions;

        if (search != null && !search.trim().isEmpty()) {
            attractions = searchAttractions(search.trim(), page, size);
        } else {
            attractions = findAttractions(page, size);
        }

        return attractions.map(this::convertToAdminDto);
    }

    private AttractionAdminDto convertToAdminDto(Attraction a) {
        return AttractionAdminDto.builder()
                .attractionId(a.getAttractionId())
                .contentId(a.getContentId())
                .title(a.getTitle())
                .addr1(a.getAddr1())
                .categoryName(getContentTypeName(a.getContentTypeId()))
                .emotionTags(getEmotionTagsForAttraction(a.getAttractionId()))
                .createdTime(a.getCreatedTime())
                .status("공개")
                .statusClass("status active")
                .build();
    }

    private String getContentTypeName(Integer contentTypeId) {
        if (contentTypeId == null) return "기타";
        Map<Integer, String> types = Map.of(
                12, "관광지", 14, "문화시설", 15, "축제공연",
                25, "여행코스", 28, "레포츠", 32, "숙박", 38, "쇼핑", 39, "음식점"
        );
        return types.getOrDefault(contentTypeId, "기타");
    }

    private String getEmotionTagsForAttraction(Long attractionId) {
        // attractionEmotionRepository를 통해 감정태그들을 가져와서 쉼표로 연결
        // 일단 빈 문자열로 처리
        return "";
    }

    @Override
    public List<AttractionCardDTO> findPopularAttractions(int limit) {
        // 1. UserAttractionRepository에서 인기순으로 정렬된 ID 목록을 가져옵니다.
        Pageable pageable = PageRequest.of(0, limit);
        List<Long> popularIds = userAttractionRepository.findPopularAttractionIds(pageable);

        if (popularIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. ID 목록을 사용하여 Attraction 엔티티들을 조회합니다. (이때 순서는 보장되지 않습니다)
        List<Attraction> unorderedAttractions = repository.findAllById(popularIds);

        // 3. 자바 코드로 직접 정렬하기 위해, 조회된 엔티티들을 ID를 키로 하는 Map으로 변환합니다.
        Map<Long, Attraction> attractionMap = unorderedAttractions.stream()
                .collect(Collectors.toMap(Attraction::getAttractionId, attraction -> attraction));

        // 4. 처음에 얻은 인기순 ID 목록(popularIds)을 순회하면서, Map에서 엔티티를 순서대로 꺼내 최종 리스트를 만듭니다.
        List<Attraction> orderedAttractions = popularIds.stream()
                .map(attractionMap::get)
                .filter(Objects::nonNull) // 혹시 모를 null 값 제거
                .collect(Collectors.toList());

        // 5. 정렬된 최종 리스트를 DTO로 변환하여 반환합니다.
        return orderedAttractions.stream()
                .map(this::mapToAttractionCardDTO)
                .collect(Collectors.toList());
    }

    // DTO 변환을 위한 헬퍼 메서드
    private AttractionCardDTO mapToAttractionCardDTO(Attraction a) {
        return AttractionCardDTO.builder()
                .contentId(a.getContentId())
                .attractionId(a.getAttractionId())
                .title(a.getTitle())
                .addr1(a.getAddr1())
                .firstImage(a.getFirstImage())
                .build();
    }



}
