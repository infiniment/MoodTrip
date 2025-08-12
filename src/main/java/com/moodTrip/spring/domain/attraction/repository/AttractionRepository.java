package com.moodTrip.spring.domain.attraction.repository;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    Optional<Attraction> findByContentId(Long contentId);

    @Query("""
        select a from Attraction a
        order by coalesce(a.modifiedTime, a.createdTime) desc, a.title asc
    """)
    List<Attraction> findTop(Pageable pageable);

    @Query("""
        select a from Attraction a
        where lower(a.title) like lower(concat('%', :q, '%'))
           or lower(a.addr1) like lower(concat('%', :q, '%'))
           or lower(coalesce(a.addr2, '')) like lower(concat('%', :q, '%'))
        order by coalesce(a.modifiedTime, a.createdTime) desc, a.title asc
    """)
    List<Attraction> searchByTitleOrAddr(@org.springframework.data.repository.query.Param("q") String q,
                                         Pageable pageable);

    // 조회 조합별로 DB에서 바로 필터링
    List<Attraction> findAllByAreaCode(int areaCode);
    List<Attraction> findAllByAreaCodeAndSigunguCode(int areaCode, int sigunguCode);
    List<Attraction> findAllByAreaCodeAndContentTypeId(int areaCode, int contentTypeId);
    List<Attraction> findAllByAreaCodeAndSigunguCodeAndContentTypeId(int areaCode, int sigunguCode, int contentTypeId);
}
