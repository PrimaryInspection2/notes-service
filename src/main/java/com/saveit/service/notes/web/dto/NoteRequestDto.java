package com.saveit.service.notes.web.dto;

import lombok.Builder;

import java.util.Set;

@Builder(toBuilder = true)
public record NoteRequestDto(String userId, String title, String content,
                             NoteSource source, NoteStatus status, NotePriority priority,
                             Set<TagDto> tags) {
}