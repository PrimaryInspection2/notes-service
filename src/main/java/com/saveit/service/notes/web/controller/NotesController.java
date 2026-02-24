package com.saveit.service.notes.web.controller;

import com.saveit.service.notes.service.NoteService;
import com.saveit.service.notes.web.dto.NoteServiceRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/note")
@RequiredArgsConstructor
public class NotesController {

    private final NoteService notesService;

    @PostMapping
    public NoteResponseDto create(@RequestBody NoteServiceRequestDto request) {
        log.info("Create note for");
        return notesService.create(request);
    }

    @GetMapping("/{id}")
    public NoteResponseDto getById(@PathVariable String id) {
        log.info("Get note id={}", id);
        return notesService.getById(id);
    }

    @PutMapping("/{id}")
    public NoteResponseDto update(@RequestBody NoteServiceRequestDto request) {
        log.info("Update note id={}", request.noteId());
        return notesService.update(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        log.info("Delete note id={}", id);
        notesService.delete(id);
    }

    @GetMapping
    public Set<NoteResponseDto> getAll(@RequestParam String userId) {
        log.info("Get all notes for userId={}", userId);
        return notesService.getAllByUserId(userId);
    }

}
