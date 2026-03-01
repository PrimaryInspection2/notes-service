package com.saveit.service.notes.mapper;

import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.util.TestDataUtil;
import com.saveit.service.notes.web.dto.TagDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagMapperTest {

    private TagMapper tagMapper;

    @BeforeEach
    void setUp() {
        tagMapper = new TagMapper();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        TagDto dto = TestDataUtil.createTagDto("tag1", "MyTag");

        TagEntity entity = tagMapper.toEntity(dto, "user1");

        assertThat(entity.getTagId()).isEqualTo("tag1");
        assertThat(entity.getName()).isEqualTo("MyTag");
        assertThat(entity.getColor()).isEqualTo("Red");
        assertThat(entity.getDescription()).isEqualTo("Description MyTag");
        assertThat(entity.getUserId()).isEqualTo("user1");
    }

    @Test
    void toDto_shouldMapAllFields() {
        TagEntity entity = TestDataUtil.createTagEntity("tag1", "MyTag", "user1");

        TagDto dto = tagMapper.toDto(entity);

        assertThat(dto.tagId()).isEqualTo("tag1");
        assertThat(dto.name()).isEqualTo("MyTag");
        assertThat(dto.color()).isEqualTo("Red");
        assertThat(dto.description()).isEqualTo("Description MyTag");
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        TagEntity existing = TestDataUtil.createTagEntity("tag1", "OldTag", "user1");
        TagDto dto = TestDataUtil.createTagDto("tag1", "UpdatedTag");

        tagMapper.updateEntity(existing, dto);

        assertThat(existing.getName()).isEqualTo("UpdatedTag");
        assertThat(existing.getColor()).isEqualTo("Red");
        assertThat(existing.getDescription()).isEqualTo("Description UpdatedTag");
    }
}