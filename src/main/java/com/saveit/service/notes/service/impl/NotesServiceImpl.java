package com.saveit.service.notes.service.impl;

import com.saveit.service.notes.mapper.NoteMapper;
import com.saveit.service.notes.repository.NoteRepository;
import com.saveit.service.notes.repository.entity.NoteEntity;
import com.saveit.service.notes.service.NoteService;
import com.saveit.service.notes.web.dto.NoteRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotesServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    @Override
    public NoteResponseDto create(NoteRequestDto request) {
        String userId = UUID.randomUUID().toString();  //fixme implement it when user-service ready
        log.info("Creating note for userId={}", userId);
        NoteEntity entity = noteMapper.toEntity(request, userId);
        NoteEntity saved = noteRepository.save(entity);
        log.info("Note created with id={}", saved.getNoteId());
        return noteMapper.toDto(saved);
    }

    @Override
    public NoteResponseDto getById(String id) {
        log.info("Fetching note with id={}", id);
        NoteEntity entity = noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Note not found: " + id));
        return noteMapper.toDto(entity);
    }

    @Override
    public NoteResponseDto update(String id, NoteRequestDto request) {
        log.info("Updating note id={}", id);
        NoteEntity existing = noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Note not found: " + id));
        noteMapper.updateEntity(existing, request);
        NoteEntity saved = noteRepository.save(existing);
        log.info("Note updated: {}", saved.getNoteId());
        return noteMapper.toDto(saved);
    }

    @Override
    public void delete(String id) {
        log.info("Deleting note with id={}", id);
        noteRepository.deleteById(id);
        log.info("Note with id={} deleted", id);
    }

    @Override
    public Set<NoteResponseDto> getAllByUserId(String userId) {
        log.info("Fetching all notes for userId={}", userId);
        Set<NoteEntity> notes = noteRepository.findAllByUserId(userId);
        log.info("Fetched {} notes for userId={}", notes.size(), userId);
        return notes.stream()
                .map(noteMapper::toDto)
                .collect(Collectors.toSet());
    }
}