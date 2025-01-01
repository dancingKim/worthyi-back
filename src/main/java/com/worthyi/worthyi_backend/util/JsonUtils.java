package com.worthyi.worthyi_backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worthyi.worthyi_backend.model.dto.ActionContentDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ActionContentDto parseActionContent(String jsonData) {
        try {
            if (jsonData == null) return null;
            
            // JSON 형식인지 확인
            if (jsonData.startsWith("{")) {
                return objectMapper.readValue(jsonData, ActionContentDto.class);
            } else {
                // 레거시 데이터인 경우 text로 처리
                return ActionContentDto.fromLegacyData(jsonData);
            }
        } catch (Exception e) {
            log.error("Action content 파싱 실패", e);
            // 파싱 실패시 레거시 형식으로 처리
            return ActionContentDto.fromLegacyData(jsonData);
        }
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to convert object to JSON", e);
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (Exception e) {
            log.error("Failed to parse JSON", e);
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
} 