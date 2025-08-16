package com.moodTrip.spring.domain.transport.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.transport.dto.response.ODsayResponse;
import com.moodTrip.spring.domain.transport.service.dto.RouteOptionDto;
import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import com.moodTrip.spring.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ODsayServiceImpl implements ODsayService {

    @Value("${odsay.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private static final ObjectMapper OM = new ObjectMapper();

    private static String normalizeKey(String raw) {
        if (raw == null) return null;
        String k = raw.trim();
        if ((k.startsWith("\"") && k.endsWith("\"")) || (k.startsWith("'") && k.endsWith("'"))) {
            k = k.substring(1, k.length() - 1);
        }
        return k.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }

    @Override
    public List<RouteOptionDto> getTransitRoutes(double sx, double sy, double ex, double ey) {
        // 1) 키 인코딩
        String encodedKey = URLEncoder.encode(normalizeKey(apiKey), StandardCharsets.UTF_8);

        // 2) 좌표를 Locale.US로 포매팅(소수점 . 보장)
        String sxStr = String.format(Locale.US, "%.8f", sx);
        String syStr = String.format(Locale.US, "%.8f", sy);
        String exStr = String.format(Locale.US, "%.8f", ex);
        String eyStr = String.format(Locale.US, "%.8f", ey);

        // 3) URL 구성 (재인코딩 금지)
        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.odsay.com/v1/api/searchPubTransPathT")
                .queryParam("SX", sxStr)
                .queryParam("SY", syStr)
                .queryParam("EX", exStr)
                .queryParam("EY", eyStr)
                .queryParam("OPT", 0)            // 선택: 경로옵션(없어도 동작, 붙이는 걸 권장)
                .queryParam("apiKey", encodedKey) // %2B 등 유지
                .build(true)
                .toUriString();

        // 키 마스킹 로그
        int idx = url.indexOf("apiKey=");
        log.debug("ODsay request: {}****", (idx > 0 ? url.substring(0, idx + 7) : url));

        try {
            String bodyStr = restTemplate.getForObject(url, String.class);
            log.debug("ODsay raw body: {}", bodyStr);

            JsonNode root = OM.readTree(bodyStr == null ? "{}" : bodyStr);

            // 4) 에러 응답(객체/배열) 처리
            if (root.has("error")) {
                JsonNode err = root.path("error");
                if (err.isArray() && err.size() > 0) err = err.get(0);

                String codeStr = err.path("code").asText("");
                String msg = err.path("message").asText(err.path("msg").asText(""));

                int code;
                try { code = Integer.parseInt(codeStr); } catch (NumberFormatException e) { code = 0; }

                log.warn("ODsay error: code={}, message={}", codeStr, msg);

                // 인증 실패 메시지 패턴 매핑(동일 500이라도 구분)
                if (code == 500 && msg != null && msg.toLowerCase().contains("auth")) {
                    throw new CustomException(ErrorStatus.TRANSPORT_AUTH_ERROR);
                }
                if (code == 429) throw new CustomException(ErrorStatus.TRANSPORT_RATE_LIMIT);
                if (code == 400) throw new CustomException(ErrorStatus.TRANSPORT_BAD_REQUEST);

                throw new CustomException(ErrorStatus.TRANSPORT_API_ERROR);
            }

            // 5) 정상 결과 매핑
            ODsayResponse body = OM.treeToValue(root, ODsayResponse.class);
            if (body == null || body.getResult() == null || body.getResult().getPath() == null
                    || body.getResult().getPath().isEmpty()) {
                throw new CustomException(ErrorStatus.TRANSPORT_NO_ROUTE);
            }

            return body.getResult().getPath().stream()
                    .limit(5)
                    .map(p -> mapToDto(p, sx, sy, ex, ey))
                    .toList();

        } catch (RestClientResponseException e) {
            log.error("ODsay HTTP error {} body={}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(ErrorStatus.TRANSPORT_API_ERROR);
        } catch (CustomException e) {
            throw e; // 위에서 매핑한 에러 그대로 전달
        } catch (Exception e) {
            log.error("ODsay request failed", e);
            throw new CustomException(ErrorStatus.TRANSPORT_API_ERROR);
        }
    }

    private RouteOptionDto mapToDto(ODsayResponse.Path p,
                                     double sx, double sy, double ex, double ey) {
        ODsayResponse.Info i = p.getInfo();
        List<ODsayResponse.SubPath> subs = p.getSubPath();

        // --- 구간 라벨링 (null-safe)
        List<String> segs = new ArrayList<>();
        int subwayCnt = 0, busCnt = 0;

        if (subs != null) {
            for (ODsayResponse.SubPath s : subs) {
                if (s == null) continue;
                Integer tt = s.getTrafficType();
                String label;
                if (tt == null) {
                    label = "이동";
                } else if (tt == 1) {
                    subwayCnt++;
                    label = "지하철";
                } else if (tt == 2) {
                    busCnt++;
                    label = "버스";
                } else if (tt == 3) {
                    label = "도보";
                } else {
                    label = "이동";
                }
                Integer m = s.getSectionTime();
                segs.add(m == null ? label : (label + " " + m + "분"));
            }
        }

        String parts = (subwayCnt > 0 ? "지하철+" : "") + (busCnt > 0 ? "버스+" : "");
        if (parts.endsWith("+")) parts = parts.substring(0, parts.length() - 1);
        if (parts.isEmpty()) parts = "대중교통";

        // 환승 수 계산 (
        int transferCount = computeTransferCount(i, subs);

        // 요약 문자열
        String summary = parts + " · 환승 " + transferCount + "회";

        // 외부(카카오맵) 링크
        String fromName = enc("출발지");
        String toName   = enc("도착지");
        String kakaoUrl =
                "https://map.kakao.com/link/from/" + fromName + "," + sy + "," + sx +
                        "/to/" + toName + "," + ey + "," + ex;

        // --- null-safe totalTime, payment
        int totalTime = i != null && i.getTotalTime() != null ? i.getTotalTime() : 0;
        int payment   = i != null && i.getPayment()   != null ? i.getPayment()   : 0;

        return RouteOptionDto.builder()
                .routeSummary(summary)
                .totalTime(totalTime)
                .fare(payment)
                .transferCount(transferCount)
                .segments(segs)
                .externalUrl(kakaoUrl)
                .build();

    }

    /**
     * 환승 수 계산:
     * 1) Info.transferCount 사용
     * 2) 없으면 busTransitCount + subwayTransitCount
     * 3) 그것도 없으면 subPath의 버스/지하철 구간 수 - 1
     */
    private int computeTransferCount(ODsayResponse.Info info, List<ODsayResponse.SubPath> subs) {
        if (info != null && info.getTransferCount() != null) {
            return Math.max(0, info.getTransferCount());
        }
        int sumByInfo = 0;
        boolean hasAny = false;
        if (info != null) {
            if (info.getBusTransitCount() != null) {
                sumByInfo += info.getBusTransitCount();
                hasAny = true;
            }
            if (info.getSubwayTransitCount() != null) {
                sumByInfo += info.getSubwayTransitCount();
                hasAny = true;
            }
        }
        if (hasAny) return Math.max(0, sumByInfo);

        if (subs == null) return 0;
        int vehicleLegs = 0;
        for (ODsayResponse.SubPath s : subs) {
            if (s == null) continue;
            Integer t = s.getTrafficType();
            if (t != null && (t == 1 || t == 2)) vehicleLegs++;
        }
        return Math.max(0, vehicleLegs - 1);
    }

    /** URL 인코딩 유틸 (이미 있다면 생략) */
    private String enc(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
