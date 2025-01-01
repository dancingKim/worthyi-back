package com.worthyi.worthyi_backend.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionContentDto {
    private String text;
    private String imageUrl;
    
    public static ActionContentDto fromLegacyData(String data) {
        return ActionContentDto.builder()
                .text(data)
                .build();
    }
} 