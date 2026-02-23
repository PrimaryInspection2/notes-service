package com.saveit.service.notes.service;


import com.saveit.service.notes.web.dto.NoteRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;

import java.util.Set;

public interface NoteService {

    NoteResponseDto create(NoteRequestDto request);

    NoteResponseDto getById(String id);

    NoteResponseDto update(String id, NoteRequestDto request);

    void delete(String id);

    Set<NoteResponseDto> getAllByUserId(String userId);
}
