package com.moodTrip.spring.domain.admin.repository;

import com.moodTrip.spring.domain.admin.entity.AdminSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminSettingsRepository extends JpaRepository<AdminSettings, Long> {
    Optional<AdminSettings> findBySettingKey(String settingKey);
    List<AdminSettings> findBySettingKeyIn(List<String> keys);
}