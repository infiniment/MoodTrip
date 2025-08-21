package com.moodTrip.spring.domain.attraction.repository;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    Optional<Attraction> findByContentId(Long contentId);

    // 단순 목록
    List<Attraction> findAllByAreaCode(Integer areaCode);
    List<Attraction> findAllByAreaCodeAndSigunguCode(Integer areaCode, Integer sigunguCode);
    List<Attraction> findAllByAreaCodeAndContentTypeId(Integer areaCode, Integer contentTypeId);
    List<Attraction> findAllByAreaCodeAndSigunguCodeAndContentTypeId(Integer areaCode, Integer sigunguCode, Integer contentTypeId);

    // 페이징 목록
    Page<Attraction> findByAreaCode(Integer areaCode, Pageable pageable);
    Page<Attraction> findByAreaCodeAndSigunguCode(Integer areaCode, Integer sigunguCode, Pageable pageable);

    // 다중 지역
    List<Attraction> findByAreaCodeIn(List<Integer> areaCodes);
    Page<Attraction> findByAreaCodeIn(List<Integer> areaCodes, Pageable pageable);

    // 키워드 검색(일반)
    Page<Attraction> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    // 키워드 검색 + 제목 시작 우선 + 필터(지역/시군구/타입)
    @Query("""
           select a
           from Attraction a
           where (:q is null or lower(a.title) like lower(concat('%', :q, '%')))
             and (:area is null or a.areaCode = :area)
             and (:si   is null or a.sigunguCode = :si)
             and (:type is null or a.contentTypeId = :type)
           order by case when :q is not null and lower(a.title) like lower(concat(:q, '%')) then 0 else 1 end,
                    a.title asc
           """)
    Page<Attraction> searchKeywordPrefTitleStarts(@Param("q") String q,
                                                  @Param("area") Integer area,
                                                  @Param("si") Integer si,
                                                  @Param("type") Integer type,
                                                  Pageable pageable);

    // 감정 태그 매핑 (스키마에 맞게 조정 필요, 실행 전용 — 컴파일용)
    @Query(value = """
            select a.* from attractions a
            join emotion_attraction ea on ea.attraction_id = a.attraction_id
            where ea.emotion_id in (:emotionIds)
            """, nativeQuery = true)
    List<Attraction> findAttractionsByEmotionIds(@Param("emotionIds") List<Integer> emotionIds);

    //광광지 카드 필터
    @Query("""
select a
from Attraction a
where (:areasEmpty = true or a.areaCode in :areas)
  and (:keyword is null or :keyword = '' or lower(a.title) like lower(concat('%', :keyword, '%')))
  and (:cat1 is null or a.cat1 = :cat1)
  and (:cat2 is null or a.cat2 = :cat2)
  and (:cat3 is null or a.cat3 = :cat3)
""")
    Page<Attraction> searchByFilters(
            @Param("areas")      List<Integer> areas,
            @Param("areasEmpty") boolean areasEmpty,
            @Param("keyword")    String keyword,
            @Param("cat1")       String cat1,
            @Param("cat2")       String cat2,
            @Param("cat3")       String cat3,
            Pageable pageable
    );
}
