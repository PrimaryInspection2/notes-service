package com.saveit.service.notes.mapper;

import com.saveit.service.notes.repository.entity.NoteEntity;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.web.dto.NoteRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;
import com.saveit.service.notes.web.dto.TagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NoteMapper {

    private final TagMapper tagMapper;

    public NoteEntity toEntity(NoteRequestDto dto, String userId) {
        NoteEntity entity = new NoteEntity();
        entity.setNoteId(UUID.randomUUID().toString());
        entity.setUserId(userId); //fixme implement it when user-service ready
        entity.setTitle(dto.title());
        entity.setContent(dto.content());
        entity.setSource(dto.source());
        entity.setStatus(dto.status());
        entity.setPriority(dto.priority());

        if (dto.tags() != null) {
            Set<TagEntity> tagEntities = dto.tags().stream()
                    .map(tagDto -> tagMapper.toEntity(tagDto, userId))
                    .collect(Collectors.toSet());
            entity.setTags(tagEntities);
        }

        return entity;
    }

    public void updateEntity(NoteEntity existing, NoteRequestDto dto) {
        existing.setTitle(dto.title());
        existing.setContent(dto.content());
        existing.setSource(dto.source());
        existing.setStatus(dto.status());
        existing.setPriority(dto.priority());

        if (dto.tags() != null) {
            Set<TagEntity> updatedTags = dto.tags().stream()
                    .map(tagDto -> tagMapper.toEntity(tagDto, existing.getUserId()))
                    .collect(Collectors.toSet());
            existing.setTags(updatedTags);
        }
    }

    public NoteResponseDto toDto(NoteEntity entity) {
        Set<TagDto> tags = entity.getTags() == null ? Collections.emptySet() :
                entity.getTags().stream()
                        .map(tagMapper::toDto)
                        .collect(Collectors.toSet());

        return new NoteResponseDto(
                entity.getNoteId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getSource(),
                entity.getStatus(),
                entity.getPriority(),
                tags,
                toLocalDateTime(entity.getCreatedAt()),
                toLocalDateTime(entity.getUpdatedAt())
        );
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}