package com.saveit.service.notes.web.controller;

import com.saveit.service.notes.service.NoteService;
import com.saveit.service.notes.web.dto.GetNotesRequestDto;
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
    public NoteResponseDto processNote(@RequestBody NoteServiceRequestDto request) {
        log.info("Processing note for userId:{}" , request.userId());
        return notesService.processNote(request);
    }

    @GetMapping("/{id}")
    public NoteResponseDto getById(@PathVariable String id) {
        log.info("Get note id={}", id);
        return notesService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        log.info("Delete note id={}", id);
        notesService.delete(id);
    }

    @GetMapping("/all")
    public Set<NoteResponseDto> getAll(@RequestBody GetNotesRequestDto  request) {
        log.info("Get all notes for userId={}", request.userId());
        return notesService.getAllByUserId(request);
    }

}
