package com.saveit.service.notes.service;

import com.saveit.service.notes.mapper.TagMapper;
import com.saveit.service.notes.repository.TagRepository;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.service.impl.TagServiceImpl;
import com.saveit.service.notes.util.TestDataUtil;
import com.saveit.service.notes.web.dto.TagDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processTags_shouldReturnEmptySet_whenInputNull() {
        Set<TagEntity> result = tagService.processTags(null, "user1");
        assertThat(result).isEmpty();
    }

    @Test
    void processTags_shouldReturnEmptySet_whenInputEmpty() {
        Set<TagEntity> result = tagService.processTags(Collections.emptySet(), "user1");
        assertThat(result).isEmpty();
    }

    @Test
    void processTags_shouldCreateNewTag_whenNotExists() {
        TagDto dto = TestDataUtil.createTagDto("tag1", "NewTag");
        TagEntity entity = TestDataUtil.createTagEntity("tag1", "NewTag", "user1");

        when(tagRepository.findAllByUserIdAndNameIn("user1", Set.of("NewTag")))
                .thenReturn(Collections.emptySet());
        when(tagMapper.toEntity(dto, "user1")).thenReturn(entity);
        when(tagRepository.save(entity)).thenReturn(entity);

        Set<TagEntity> result = tagService.processTags(Set.of(dto), "user1");

        assertThat(result).containsExactly(entity);
        verify(tagMapper, never()).updateEntity(any(), any());
        verify(tagRepository).save(entity);
    }

    @Test
    void processTags_shouldUpdateExistingTag_whenExists() {
        TagDto dto = TestDataUtil.createTagDto("tag1", "ExistTag");
        TagEntity existing = TestDataUtil.createTagEntity("tag1", "ExistTag", "user1");

        when(tagRepository.findAllByUserIdAndNameIn("user1", Set.of("ExistTag")))
                .thenReturn(Set.of(existing));
        when(tagRepository.save(existing)).thenReturn(existing);

        Set<TagEntity> result = tagService.processTags(Set.of(dto), "user1");

        assertThat(result).containsExactly(existing);
        verify(tagMapper).updateEntity(existing, dto);
        verify(tagRepository).save(existing);
    }

    @Test
    void processTags_shouldHandleMultipleTags_mixOfNewAndExisting() {
        TagDto newTag = TestDataUtil.createTagDto("tag2", "NewTag");
        TagDto existingTagDto = TestDataUtil.createTagDto("tag1", "ExistTag");

        TagEntity existingEntity = TestDataUtil.createTagEntity("tag1", "ExistTag", "user1");
        TagEntity newEntity = TestDataUtil.createTagEntity("tag2", "NewTag", "user1");

        when(tagRepository.findAllByUserIdAndNameIn("user1", Set.of("ExistTag", "NewTag")))
                .thenReturn(Set.of(existingEntity));
        when(tagMapper.toEntity(newTag, "user1")).thenReturn(newEntity);
        when(tagRepository.save(newEntity)).thenReturn(newEntity);
        when(tagRepository.save(existingEntity)).thenReturn(existingEntity);

        Set<TagEntity> result = tagService.processTags(Set.of(existingTagDto, newTag), "user1");

        assertThat(result).containsExactlyInAnyOrder(existingEntity, newEntity);
        verify(tagMapper).updateEntity(existingEntity, existingTagDto);
        verify(tagRepository).save(existingEntity);
        verify(tagRepository).save(newEntity);
    }

    @Test
    void processTags_shouldHandleDuplicateNamesInRequest() {
        TagDto tag1 = TestDataUtil.createTagDto("tag1", "DupTag");
        TagDto tag2 = TestDataUtil.createTagDto("tag2", "DupTag");

        TagEntity existing = TestDataUtil.createTagEntity("tag1", "DupTag", "user1");

        when(tagRepository.findAllByUserIdAndNameIn("user1", Set.of("DupTag")))
                .thenReturn(Set.of(existing));
        when(tagRepository.save(existing)).thenReturn(existing);

        Set<TagEntity> result = tagService.processTags(Set.of(tag1, tag2), "user1");

        assertThat(result).contains(existing);

        verify(tagMapper, times(1)).updateEntity(eq(existing), argThat(dto -> "DupTag".equals(dto.name())));

        verify(tagRepository).save(existing);
    }

    @Test
    void processTags_shouldPropagateExceptionFromRepositorySave() {
        TagDto tagDto = TestDataUtil.createTagDto("tag1", "FailTag");

        when(tagRepository.findAllByUserIdAndNameIn("user1", Set.of("FailTag")))
                .thenReturn(Collections.emptySet());
        when(tagMapper.toEntity(tagDto, "user1")).thenReturn(new TagEntity());
        when(tagRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> tagService.processTags(Set.of(tagDto), "user1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}