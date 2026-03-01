package com.saveit.service.notes.service.impl;

import com.saveit.service.notes.mapper.NoteMapper;
import com.saveit.service.notes.repository.NoteRepository;
import com.saveit.service.notes.repository.entity.NoteEntity;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.service.NoteService;
import com.saveit.service.notes.web.dto.GetNotesRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;
import com.saveit.service.notes.web.dto.NoteServiceRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final TagService tagService;

    @Override
    @Transactional
    public NoteResponseDto processNote(NoteServiceRequestDto noteRequest) {
        return noteRepository.findById(noteRequest.noteId())
                .map(existing -> updateNote(existing, noteRequest))
                .orElseGet(() -> createNote(noteRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponseDto getById(String id) {
        log.info("Fetching note with id={}", id);
        NoteEntity entity = noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Note not found: " + id));
        return noteMapper.toDto(entity);
    }

    //todo to be implemented
    @Override
    public void delete(String id) {
        log.info("Deleting note with id={}", id);
        noteRepository.deleteById(id);
        log.info("Note with id={} deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<NoteResponseDto> getAllByUserId(GetNotesRequestDto request) {
        String userId = request.userId();
        Set<String> tagIds = request.tagIds();

        log.info("Fetching notes for userId={}, tagFilterSize={}", userId, tagIds.size());

        Set<NoteEntity> notes = tagIds.isEmpty()
                ? noteRepository.findAllByUserId(userId)
                : noteRepository.findDistinctByUserIdAndTags_TagIdIn(userId, tagIds);

        log.info("Fetched {} notes for userId={}", notes.size(), userId);

        return notes.stream()
                .map(noteMapper::toDto)
                .collect(Collectors.toSet());
    }

    private NoteResponseDto createNote(NoteServiceRequestDto dto) {
        Set<TagEntity> processedTags = tagService.processTags(dto.tags(), dto.userId());

        NoteEntity entity = noteMapper.toEntity(dto);
        entity.setTags(processedTags);

        NoteEntity saved = noteRepository.saveAndFlush(entity);

        return noteMapper.toDto(saved);
    }

    private NoteResponseDto updateNote(NoteEntity existing, NoteServiceRequestDto dto) {
        Set<TagEntity> updatedTags = tagService.processTags(dto.tags(), existing.getUserId());

        noteMapper.updateEntity(existing, dto);
        existing.setTags(updatedTags);

        NoteEntity saved = noteRepository.saveAndFlush(existing);

        return noteMapper.toDto(saved);
    }
}