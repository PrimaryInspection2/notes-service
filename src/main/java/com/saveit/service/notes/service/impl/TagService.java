package com.saveit.service.notes.service.impl;

import com.saveit.service.notes.mapper.TagMapper;
import com.saveit.service.notes.repository.TagRepository;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.web.dto.TagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Transactional
    public Set<TagEntity> processTags(Set<TagDto> requestDto, String userId) {
        if (requestDto == null || requestDto.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> tagNames = requestDto.stream()
                .map(TagDto::name)
                .collect(Collectors.toSet());

        Map<String, TagEntity> existingTags = tagRepository
                .findAllByUserIdAndNameIn(userId, tagNames)
                .stream()
                .collect(Collectors.toMap(TagEntity::getName, Function.identity()));

        return mergeTags(requestDto, existingTags, userId);
    }

    private Set<TagEntity> mergeTags(Set<TagDto> requestDto,
                                     Map<String, TagEntity> existingTags,
                                     String userId) {
        return requestDto.stream()
                .map(tagFromRequest -> {
                    TagEntity existingTag = existingTags.get(tagFromRequest.name());
                    return existingTag != null
                            ? updateTagEntity(existingTag, tagFromRequest)
                            : createTagEntity(tagFromRequest, userId);
                })
                .collect(Collectors.toSet());
    }

    private TagEntity createTagEntity(TagDto dto, String userId) {
        TagEntity entity = tagMapper.toEntity(dto, userId);
        return tagRepository.save(entity);
    }

    private TagEntity updateTagEntity(TagEntity existing, TagDto dto) {
        tagMapper.updateEntity(existing, dto);
        return tagRepository.save(existing);
    }
}