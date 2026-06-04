package com.giftcard.controller;

import com.giftcard.common.Result;
import com.giftcard.entity.Setting;
import com.giftcard.mapper.SettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SettingsController {

    @Autowired
    private SettingMapper settingMapper;

    /**
     * 公开接口：获取前台展示用的网站设置（名称、logo等）
     */
    @GetMapping("/site-settings")
    public Result<Map<String, Object>> getPublicSettings() {
        List<Setting> settingsList = settingMapper.selectAll();

        Map<String, Object> result = new HashMap<>();
        for (Setting setting : settingsList) {
            String key = setting.getSettingKey();
            if (key.startsWith("site_name_")) {
                // 按语言分别存储
                String lang = key.replace("site_name_", "");
                result.put("site_name_" + lang, setting.getSettingValue());
                // 同时存一个不带语言后缀的（取第一个）
                if (!result.containsKey("site_name")) {
                    result.put("site_name", setting.getSettingValue());
                }
            } else if ("site_logo".equals(key)) {
                result.put("site_logo", setting.getSettingValue());
            } else {
                result.put(key, setting.getSettingValue());
            }
        }

        // 默认值
        if (!result.containsKey("site_name")) {
            result.put("site_name", "礼品卡发卡平台");
        }
        if (!result.containsKey("site_logo")) {
            result.put("site_logo", "");
        }

        return Result.success(result);
    }

    /**
     * 管理后台：获取所有设置
     */
    @GetMapping("/admin/settings")
    public Result<Map<String, Object>> getAllSettings() {
        List<Setting> settingsList = settingMapper.selectAll();

        Map<String, Object> result = new HashMap<>();
        for (Setting setting : settingsList) {
            String key = setting.getSettingKey();
            if (key.startsWith("site_name_")) {
                if (!result.containsKey("site_name")) {
                    result.put("site_name", setting.getSettingValue());
                }
            } else if (key.startsWith("site_description_")) {
                if (!result.containsKey("site_description")) {
                    result.put("site_description", setting.getSettingValue());
                }
            } else {
                result.put(key, setting.getSettingValue());
            }
        }

        // 设置默认值
        if (!result.containsKey("site_name")) {
            result.put("site_name", "礼品卡发卡平台");
        }
        if (!result.containsKey("default_lang")) {
            result.put("default_lang", "zh");
        }
        if (!result.containsKey("order_expire")) {
            result.put("order_expire", "30");
        }
        if (!result.containsKey("site_logo")) {
            result.put("site_logo", "");
        }

        return Result.success(result);
    }

    /**
     * 管理后台：更新设置
     */
    @PutMapping("/admin/settings")
    public Result<String> updateSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if ("site_name".equals(key)) {
                updateSetting("site_name_zh", value);
                updateSetting("site_name_en", value);
                updateSetting("site_name_ja", value);
                updateSetting("site_name_ko", value);
            } else if ("site_logo".equals(key)) {
                updateSetting("site_logo", value);
            } else if ("default_lang".equals(key)) {
                updateSetting("default_lang", value);
            } else if ("order_expire".equals(key)) {
                updateSetting("order_expire", value);
            } else {
                updateSetting(key, value);
            }
        }

        return Result.success("Settings updated successfully", null);
    }

    private void updateSetting(String key, String value) {
        Setting existing = settingMapper.selectByKey(key);
        if (existing != null) {
            existing.setSettingValue(value);
            existing.setUpdatedAt(LocalDateTime.now());
            settingMapper.updateById(existing);
        } else {
            Setting newSetting = new Setting();
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            newSetting.setCreatedAt(LocalDateTime.now());
            newSetting.setUpdatedAt(LocalDateTime.now());
            settingMapper.insert(newSetting);
        }
    }
}
