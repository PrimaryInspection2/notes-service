package com.saveit.service.notes.mapper;

import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.web.dto.TagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagMapper {

    public TagEntity toEntity(TagDto dto, String userId) {
        TagEntity entity = new TagEntity();
        entity.setTagId(dto.tagId());
        entity.setUserId(userId); //fixme implement it when user-service ready
        entity.setName(dto.name());
        entity.setColor(dto.color());
        entity.setDescription(dto.description());
        return entity;
    }

    public TagDto toDto(TagEntity entity) {
        return new TagDto(
                entity.getTagId(),
                entity.getName(),
                entity.getColor(),
                entity.getDescription()
        );
    }

    public void updateEntity(TagEntity existing, TagDto dto) {
        existing.setName(dto.name());
        existing.setColor(dto.color());
        existing.setDescription(dto.description());
    }
}