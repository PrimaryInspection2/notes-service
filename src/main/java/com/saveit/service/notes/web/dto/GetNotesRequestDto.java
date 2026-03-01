package com.saveit.service.notes.web.dto;

import lombok.Builder;

import java.util.Set;

@Builder(toBuilder = true)
public record GetNotesRequestDto(String userId, Set<String> tagIds) {
}
