package com.saveit.service.notes.service;


import com.saveit.service.notes.web.dto.GetNotesRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;
import com.saveit.service.notes.web.dto.NoteServiceRequestDto;

import java.util.Set;

public interface NoteService {

    NoteResponseDto processNote(NoteServiceRequestDto request);

    NoteResponseDto getById(String id);

    void delete(String id);

    Set<NoteResponseDto> getAllByUserId(GetNotesRequestDto request);

}
