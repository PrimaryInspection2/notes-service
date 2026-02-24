package com.saveit.service.notes.service.impl;

import com.saveit.service.notes.mapper.NoteMapper;
import com.saveit.service.notes.repository.NoteRepository;
import com.saveit.service.notes.repository.entity.NoteEntity;
import com.saveit.service.notes.service.NoteService;
import com.saveit.service.notes.web.dto.NoteResponseDto;
import com.saveit.service.notes.web.dto.NoteServiceRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotesServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    @Override
    public NoteResponseDto create(NoteServiceRequestDto request) {
        log.info("Creating note for userId={}", request.userId());
        NoteEntity entity = noteMapper.toEntity(request);
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
    public NoteResponseDto update(NoteServiceRequestDto request) {
        String noteId = request.noteId();
        log.info("Updating note id={}", noteId);
        NoteEntity existing = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found: " + noteId));
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