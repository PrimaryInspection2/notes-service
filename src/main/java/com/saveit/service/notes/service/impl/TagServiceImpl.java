package com.saveit.service.notes.service.impl;

import com.saveit.service.notes.mapper.TagMapper;
import com.saveit.service.notes.repository.TagRepository;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.service.TagService;
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
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;


    @Override
    @Transactional
    public Set<TagEntity> processTags(Set<TagDto> requestDto, String userId) {
        if (requestDto == null || requestDto.isEmpty()) {
            return Collections.emptySet();
        }

        // fetch existing tags for user once
        Map<String, TagEntity> existingById = tagRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(TagEntity::getTagId, Function.identity()));

        Map<String, TagEntity> existingByName = tagRepository.findAllByUserId(userId)
                .stream()
                .collect(Collectors.toMap(TagEntity::getName, Function.identity()));

        // deduplicate by name in request to avoid creating duplicate tags in one request
        Map<String, TagDto> uniqueByName = requestDto.stream()
                .filter(t -> t.name() != null && !t.name().isBlank())
                .collect(Collectors.toMap(TagDto::name, t -> t, (first, second) -> first));

        return uniqueByName.values().stream()
                .map(tag -> resolveTag(tag, userId, existingById, existingByName))
                .collect(Collectors.toSet());
    }

    /**
     * Resolve tag: update if exists (by ID or name), otherwise create new
     */
    private TagEntity resolveTag(TagDto tag, String userId,
                                 Map<String, TagEntity> existingById,
                                 Map<String, TagEntity> existingByName) {

        // 1. Update by ID if present
        if (tag.tagId() != null && existingById.containsKey(tag.tagId())) {
            return updateTagEntity(existingById.get(tag.tagId()), tag);
        }

        // 2. Update existing tag by name if it already exists for the user
        //    Edge-case: client sends { tagId: null, name: "work" } but a tag with this name already exists.
        //    We update the existing tag instead of creating a new one. //fixme when finalized service contract (validation of tagId and create/update flows)
        TagEntity existing = existingByName.get(tag.name());
        if (existing != null) {
            return updateTagEntity(existing, tag);
        }

        // 3. Otherwise create new
        return createTagEntity(tag, userId);
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