package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.entity.AdminSettings;
import com.moodTrip.spring.domain.admin.repository.AdminSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminSettingsService {

    private final AdminSettingsRepository adminSettingsRepository;

    @Transactional(readOnly = true)
    public Map<String, String> getSettings() {
        List<String> keys = List.of("admin_email","login_attempt_limit","session_timeout_minutes");
        List<AdminSettings> settings = adminSettingsRepository.findBySettingKeyIn(keys);

        Map<String, String> result = new HashMap<>();
        for (AdminSettings s : settings) {
            result.put(s.getSettingKey(), s.getSettingValue());
        }
        result.putIfAbsent("admin_email", "admin@travel.com");
        result.putIfAbsent("login_attempt_limit", "5");
        result.putIfAbsent("session_timeout_minutes", "30");
        return result;
    }

    // 필터에서 쓰기 좋은 헬퍼
    @Transactional(readOnly = true)
    public int getTimeoutMinutesSafe() {
        String v = getSettings().get("session_timeout_minutes");
        try {
            int m = Integer.parseInt(v);
            return (m > 0 ? m : 30);
        } catch (Exception e) {
            return 30;
        }
    }

    @Transactional
    public void saveSetting(String key, String value) {
        Optional<AdminSettings> existing = adminSettingsRepository.findBySettingKey(key);
        if (existing.isPresent()) {
            existing.get().setSettingValue(value);
        } else {
            AdminSettings newSetting = AdminSettings.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .build();
            adminSettingsRepository.save(newSetting);
        }
    }

    @Transactional
    public void resetToDefaults() {
        saveSetting("admin_email", "admin@travel.com");
        saveSetting("login_attempt_limit", "5");
        saveSetting("session_timeout_minutes", "30");
    }
}
