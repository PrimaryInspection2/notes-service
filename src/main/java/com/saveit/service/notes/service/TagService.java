package com.saveit.service.notes.service;

import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.web.dto.TagDto;

import java.util.Set;

public interface TagService {

    Set<TagEntity> processTags(Set<TagDto> requestDto, String userId);

}