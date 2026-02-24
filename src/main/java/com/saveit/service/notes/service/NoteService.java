package com.saveit.service.notes.service;


import com.saveit.service.notes.web.dto.NoteServiceRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;

import java.util.Set;

public interface NoteService {

    NoteResponseDto create(NoteServiceRequestDto request);

    NoteResponseDto getById(String id);

    NoteResponseDto update(NoteServiceRequestDto request);

    void delete(String id);

    Set<NoteResponseDto> getAllByUserId(String userId);
}
