package com.moodTrip.spring.domain.admin.repository;

import com.moodTrip.spring.domain.admin.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByCategory(String category);
    List<Faq> findByTitleContainingOrContentContaining(String title, String content);
    //테스트용
    @Query("SELECT f FROM Faq f WHERE f.title LIKE %:query% OR f.content LIKE %:query%")
    List<Faq> searchByQuery(@Param("query") String query);
}