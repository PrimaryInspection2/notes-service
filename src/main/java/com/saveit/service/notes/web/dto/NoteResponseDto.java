package com.saveit.service.notes.web.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder(toBuilder = true)
public record NoteResponseDto(String noteId,String userId, String title, String content,
                              NoteSource source, NoteStatus status, NotePriority priority,
                              Set<TagDto> tags,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
}
