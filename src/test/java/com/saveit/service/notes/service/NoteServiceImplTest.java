package com.saveit.service.notes.service;

import com.saveit.service.notes.mapper.NoteMapper;
import com.saveit.service.notes.repository.NoteRepository;
import com.saveit.service.notes.repository.entity.NoteEntity;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.service.impl.NoteServiceImpl;
import com.saveit.service.notes.service.impl.TagServiceImpl;
import com.saveit.service.notes.util.TestDataUtil;
import com.saveit.service.notes.web.dto.GetNotesRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;
import com.saveit.service.notes.web.dto.NoteServiceRequestDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;
import java.util.Set;

import static com.saveit.service.notes.util.TestDataUtil.createNoteRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private TagServiceImpl tagService;

    @InjectMocks
    private NoteServiceImpl noteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processNote_shouldCreateNewNote_whenNotExists() {
        NoteServiceRequestDto dto = createNoteRequest("note1", "user1");
        NoteEntity entity = TestDataUtil.createNoteEntity("note1", "user1");
        NoteResponseDto response = TestDataUtil.createNoteResponse("note1", "user1");

        when(noteRepository.findById("note1")).thenReturn(Optional.empty());
        when(tagService.processTags(dto.tags(), "user1")).thenReturn(Set.of());
        when(noteMapper.toEntity(dto)).thenReturn(entity);
        when(noteRepository.saveAndFlush(entity)).thenReturn(entity);
        when(noteMapper.toDto(entity)).thenReturn(response);

        NoteResponseDto result = noteService.processNote(dto);

        assertThat(result).isEqualTo(response);
        verify(tagService).processTags(dto.tags(), "user1");
        verify(noteRepository).saveAndFlush(entity);
        verify(noteMapper).toDto(entity);
    }

    @Test
    void processNote_shouldUpdateExistingNote_whenExists() {
        NoteServiceRequestDto dto = createNoteRequest("note1", "user1");
        NoteEntity existing = TestDataUtil.createNoteEntity("note1", "user1");
        NoteResponseDto response = TestDataUtil.createNoteResponse("note1", "user1");

        when(noteRepository.findById("note1")).thenReturn(Optional.of(existing));
        when(tagService.processTags(dto.tags(), "user1")).thenReturn(Set.of());
        when(noteRepository.saveAndFlush(existing)).thenReturn(existing);
        when(noteMapper.toDto(existing)).thenReturn(response);

        NoteResponseDto result = noteService.processNote(dto);

        assertThat(result).isEqualTo(response);
        verify(tagService).processTags(dto.tags(), "user1");
        verify(noteMapper).updateEntity(existing, dto);
        verify(noteRepository).saveAndFlush(existing);
    }

    @Test
    void getById_shouldReturnNote() {
        NoteEntity entity = TestDataUtil.createNoteEntity("note1", "user1");
        NoteResponseDto response = TestDataUtil.createNoteResponse("note1", "user1");

        when(noteRepository.findById("note1")).thenReturn(Optional.of(entity));
        when(noteMapper.toDto(entity)).thenReturn(response);

        NoteResponseDto result = noteService.getById("note1");

        assertThat(result).isEqualTo(response);
        verify(noteRepository).findById("note1");
        verify(noteMapper).toDto(entity);
    }

    @Test
    void delete_shouldCallRepository() {
        doNothing().when(noteRepository).deleteById("note1");

        noteService.delete("note1");

        verify(noteRepository).deleteById("note1");
    }

    @Test
    void getAllByUserId_shouldReturnMappedNotes() {
        NoteEntity entity = TestDataUtil.createNoteEntity("note1", "user1");
        NoteResponseDto dto = TestDataUtil.createNoteResponse("note1", "user1");

        when(noteRepository.findAllByUserId("user1")).thenReturn(Set.of(entity));
        when(noteMapper.toDto(entity)).thenReturn(dto);

        Set<NoteResponseDto> result = noteService.getAllByUserId(TestDataUtil.createGetNotesRequest("user1"));

        assertThat(result).containsExactly(dto);
        verify(noteRepository).findAllByUserId("user1");
        verify(noteMapper).toDto(entity);
    }

    @Test
    void processNote_shouldProcessTagsWhenCreating() {
        NoteServiceRequestDto dto = createNoteRequest("note1", "user1");
        NoteEntity entity = TestDataUtil.createNoteEntity("note1", "user1");
        NoteResponseDto response = TestDataUtil.createNoteResponse("note1", "user1");
        Set<TagEntity> processedTags = Set.of(TestDataUtil.createTagEntity("tag1", "Tag1", "user1"));

        when(noteRepository.findById("note1")).thenReturn(Optional.empty());
        when(tagService.processTags(dto.tags(), "user1")).thenReturn(processedTags);
        when(noteMapper.toEntity(dto)).thenReturn(entity);
        when(noteRepository.saveAndFlush(entity)).thenReturn(entity);
        when(noteMapper.toDto(entity)).thenReturn(response);

        NoteResponseDto result = noteService.processNote(dto);

        assertThat(result).isEqualTo(response);
        verify(tagService).processTags(dto.tags(), "user1");
        assertThat(entity.getTags()).isEqualTo(processedTags);
    }

    @Test
    void processNote_shouldProcessTagsWhenUpdating() {
        NoteServiceRequestDto dto = createNoteRequest("note1", "user1");
        NoteEntity existing = TestDataUtil.createNoteEntity("note1", "user1");
        NoteResponseDto response = TestDataUtil.createNoteResponse("note1", "user1");
        Set<TagEntity> updatedTags = Set.of(TestDataUtil.createTagEntity("tag1", "Tag1", "user1"));

        when(noteRepository.findById("note1")).thenReturn(Optional.of(existing));
        when(tagService.processTags(dto.tags(), "user1")).thenReturn(updatedTags);
        when(noteRepository.saveAndFlush(existing)).thenReturn(existing);
        when(noteMapper.toDto(existing)).thenReturn(response);

        NoteResponseDto result = noteService.processNote(dto);

        assertThat(result).isEqualTo(response);
        verify(tagService).processTags(dto.tags(), "user1");
        assertThat(existing.getTags()).isEqualTo(updatedTags);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotExists() {
        when(noteRepository.findById("note1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getById("note1"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void delete_shouldThrowException_whenRepositoryFails() {
        doThrow(new RuntimeException("Delete failed")).when(noteRepository).deleteById("note1");

        assertThatThrownBy(() -> noteService.delete("note1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Delete failed");
    }

    @Test
    void getAllByUserId_shouldFilterByTags() {
        NoteEntity entity = TestDataUtil.createNoteEntity("note1", "user1");
        NoteResponseDto dto = TestDataUtil.createNoteResponse("note1", "user1");
        GetNotesRequestDto request = new GetNotesRequestDto("user1", Set.of("tag1"));

        when(noteRepository.findDistinctByUserIdAndTags_TagIdIn("user1", Set.of("tag1")))
                .thenReturn(Set.of(entity));
        when(noteMapper.toDto(entity)).thenReturn(dto);

        Set<NoteResponseDto> result = noteService.getAllByUserId(request);

        assertThat(result).containsExactly(dto);
        verify(noteRepository).findDistinctByUserIdAndTags_TagIdIn("user1", Set.of("tag1"));
    }
}