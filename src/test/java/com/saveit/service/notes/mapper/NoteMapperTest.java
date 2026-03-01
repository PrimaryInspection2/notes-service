package com.saveit.service.notes.mapper;

import com.saveit.service.notes.repository.entity.NoteEntity;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.util.TestDataUtil;
import com.saveit.service.notes.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.saveit.service.notes.util.TestDataUtil.createNoteRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NoteMapperTest {

    private NoteMapper noteMapper;
    private TagMapper tagMapper;

    @BeforeEach
    void setUp() {
        tagMapper = mock(TagMapper.class);
        noteMapper = new NoteMapper(tagMapper);
    }

    @Test
    void toEntity_shouldMapFieldsWithoutTags() {
        NoteServiceRequestDto dto = createNoteRequest("note1", "user1");

        NoteEntity entity = noteMapper.toEntity(dto);

        assertThat(entity.getNoteId()).isEqualTo("note1");
        assertThat(entity.getUserId()).isEqualTo("user1");
        assertThat(entity.getTitle()).isEqualTo(dto.title());
        assertThat(entity.getContent()).isEqualTo(dto.content());
        assertThat(entity.getStatus()).isEqualTo(dto.status());
        assertThat(entity.getPriority()).isEqualTo(dto.priority());
        assertThat(entity.getTags()).isEmpty();
    }

    @Test
    void toEntity_shouldMapTags() {
        TagDto tagDto = TestDataUtil.createTagDto("tag1", "Tag1");
        NoteServiceRequestDto dto = createNoteRequest("note1", "user1")
                .toBuilder().tags(Set.of(tagDto)).build();

        TagEntity tagEntity = TestDataUtil.createTagEntity("tag1", "Tag1", "user1");
        when(tagMapper.toEntity(tagDto, "user1")).thenReturn(tagEntity);

        NoteEntity entity = noteMapper.toEntity(dto);

        assertThat(entity.getTags()).containsExactly(tagEntity);
        verify(tagMapper).toEntity(tagDto, "user1");
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        NoteEntity entity = TestDataUtil.createNoteEntity("note1", "user1");
        NoteServiceRequestDto dto = createNoteRequest("note1", "user1")
                .toBuilder()
                .title("Updated Title")
                .content("Updated Content")
                .build();

        noteMapper.updateEntity(entity, dto);

        assertThat(entity.getTitle()).isEqualTo("Updated Title");
        assertThat(entity.getContent()).isEqualTo("Updated Content");
    }

    @Test
    void toDto_shouldMapFieldsAndTags() {
        TagEntity tagEntity = TestDataUtil.createTagEntity("tag1", "Tag1", "user1");
        NoteEntity entity = TestDataUtil.createNoteEntityWithTags("note1", "user1", Set.of(tagEntity));

        TagDto tagDto = TestDataUtil.createTagDto("tag1", "Tag1");
        when(tagMapper.toDto(tagEntity)).thenReturn(tagDto);

        NoteResponseDto dto = noteMapper.toDto(entity);

        assertThat(dto.noteId()).isEqualTo("note1");
        assertThat(dto.userId()).isEqualTo("user1");
        assertThat(dto.tags()).containsExactly(tagDto);
        verify(tagMapper).toDto(tagEntity);
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        NoteEntity entity = TestDataUtil.createNoteEntity("note1", "user1");
        NoteServiceRequestDto dto = createNoteRequest("note1", "user1")
                .toBuilder()
                .title("Updated Title")
                .content("Updated Content")
                .status(NoteStatus.ARCHIVED)
                .priority(NotePriority.HIGH)
                .source(NoteSource.REST_API)
                .build();

        noteMapper.updateEntity(entity, dto);

        assertThat(entity.getTitle()).isEqualTo("Updated Title");
        assertThat(entity.getContent()).isEqualTo("Updated Content");
        assertThat(entity.getStatus()).isEqualTo(NoteStatus.ARCHIVED);
        assertThat(entity.getPriority()).isEqualTo(NotePriority.HIGH);
        assertThat(entity.getSource()).isEqualTo(NoteSource.REST_API);
    }
}