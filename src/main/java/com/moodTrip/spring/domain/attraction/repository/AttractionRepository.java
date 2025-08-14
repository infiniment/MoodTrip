package com.moodTrip.spring.domain.attraction.repository;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    Optional<Attraction> findByContentId(Long contentId);

    /** 최신순 상위 n개 */
    @Query("""
        select a from Attraction a
        order by coalesce(a.modifiedTime, a.createdTime) desc, a.title asc
    """)
    List<Attraction> findTop(Pageable pageable);

    /** 키워드 검색 (제목+주소) - 리스트 */
    @Query("""
        select a from Attraction a
        where lower(a.title) like lower(concat('%', :q, '%'))
           or lower(a.addr1) like lower(concat('%', :q, '%'))
           or lower(coalesce(a.addr2, '')) like lower(concat('%', :q, '%'))
        order by coalesce(a.modifiedTime, a.createdTime) desc, a.title asc
    """)
    List<Attraction> searchByTitleOrAddr(@Param("q") String q);

    /** 필터 전용 (area/sigungu/type 조합) - 페이지네이션 */
    @Query("""
      select a from Attraction a
      where (:areaCode is null or a.areaCode = :areaCode)
        and (:sigunguCode is null or a.sigunguCode = :sigunguCode)
        and (:contentTypeId is null or a.contentTypeId = :contentTypeId)
      order by coalesce(a.modifiedTime, a.createdTime) desc, a.title asc
    """)
    Page<Attraction> findAllFiltered(
            @Param("areaCode") Integer areaCode,
            @Param("sigunguCode") Integer sigunguCode,
            @Param("contentTypeId") Integer contentTypeId,
            Pageable pageable
    );

    /** 필터+키워드 검색 */
    @Query(value = """
      select a from Attraction a
      where (:areaCode is null or a.areaCode = :areaCode)
        and (:sigunguCode is null or a.sigunguCode = :sigunguCode)
        and (:contentTypeId is null or a.contentTypeId = :contentTypeId)
        and (
             :q is null or :q = '' or
             lower(a.title) like lower(concat('%', :q, '%')) or
             lower(a.addr1) like lower(concat('%', :q, '%')) or
             lower(coalesce(a.addr2, '')) like lower(concat('%', :q, '%')) or
             lower(coalesce(a.tel, ''))  like lower(concat('%', :q, '%'))
        )
      order by coalesce(a.modifiedTime, a.createdTime) desc, a.title asc
    """,
            countQuery = """
      select count(a) from Attraction a
      where (:areaCode is null or a.areaCode = :areaCode)
        and (:sigunguCode is null or a.sigunguCode = :sigunguCode)
        and (:contentTypeId is null or a.contentTypeId = :contentTypeId)
        and (
             :q is null or :q = '' or
             lower(a.title) like lower(concat('%', :q, '%')) or
             lower(a.addr1) like lower(concat('%', :q, '%')) or
             lower(coalesce(a.addr2, '')) like lower(concat('%', :q, '%')) or
             lower(coalesce(a.tel, ''))  like lower(concat('%', :q, '%'))
        )
    """)
    Page<Attraction> searchFilteredKeyword(
            @Param("q") String q,
            @Param("areaCode") Integer areaCode,
            @Param("sigunguCode") Integer sigunguCode,
            @Param("contentTypeId") Integer contentTypeId,
            Pageable pageable
    );

    /** 키워드 검색 시 제목 앞글자 매치 우선 */
    @Query(value = """
    select a from Attraction a
    where (:areaCode is null or a.areaCode = :areaCode)
      and (:sigunguCode is null or a.sigunguCode = :sigunguCode)
      and (:contentTypeId is null or a.contentTypeId = :contentTypeId)
      and (
           :q is null or :q = '' or
           lower(a.title) like lower(concat('%', :q, '%'))
      )
    order by
      case
        when (:q is null or :q = '') then 1
        when lower(a.title) like lower(concat(:q, '%')) then 0 else 1
      end,
      coalesce(a.modifiedTime, a.createdTime) desc, a.title asc
    """,
            countQuery = """
    select count(a) from Attraction a
    where (:areaCode is null or a.areaCode = :areaCode)
      and (:sigunguCode is null or a.sigunguCode = :sigunguCode)
      and (:contentTypeId is null or a.contentTypeId = :contentTypeId)
      and (
           :q is null or :q = '' or
           lower(a.title) like lower(concat('%', :q, '%'))
      )
    """)
    Page<Attraction> searchKeywordPrefTitleStarts(
            @Param("q") String q,
            @Param("areaCode") Integer areaCode,
            @Param("sigunguCode") Integer sigunguCode,
            @Param("contentTypeId") Integer contentTypeId,
            Pageable pageable
    );

    // 조회 조합별로 DB에서 바로 필터링
    List<Attraction> findAllByAreaCode(int areaCode);
    List<Attraction> findAllByAreaCodeAndSigunguCode(int areaCode, int sigunguCode);
    List<Attraction> findAllByAreaCodeAndContentTypeId(int areaCode, int contentTypeId);
    List<Attraction> findAllByAreaCodeAndSigunguCodeAndContentTypeId(int areaCode, int sigunguCode, int contentTypeId);

    //  선택된 감정 ID 목록을 기반으로 관련 여행지를 조회하는 쿼리
    @Query("SELECT ae.attraction " +
            "FROM AttractionEmotion ae " +
            "WHERE ae.isActive = true AND ae.emotion.tagId IN :emotionIds " +
            "GROUP BY ae.attraction.id " +
            "ORDER BY COUNT(ae.attraction.id) DESC, SUM(ae.weight) DESC")
    List<Attraction> findAttractionsByEmotionIds(@Param("emotionIds") List<Integer> emotionIds);

}
