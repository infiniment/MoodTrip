package com.moodTrip.spring.global.util;

import java.util.Map;

public class AreaCodeMapper {
    private static final Map<Integer, String> AREA_MAP = Map.ofEntries(
            Map.entry(1, "서울"),
            Map.entry(2, "인천"),
            Map.entry(3, "대전"),
            Map.entry(4, "대구"),
            Map.entry(5, "광주"),
            Map.entry(6, "부산"),
            Map.entry(7, "울산"),
            Map.entry(8, "세종"),
            Map.entry(31, "경기"),
            Map.entry(32, "강원"),
            Map.entry(33, "충북"),
            Map.entry(34, "충남"),
            Map.entry(35, "경북"),
            Map.entry(36, "경남"),
            Map.entry(37, "전북"),
            Map.entry(38, "전남"),
            Map.entry(39, "제주")
    );

    public static String getAreaName(Integer code) {
        if (code == null) return "-";
        return AREA_MAP.getOrDefault(code, "기타");
    }
}
