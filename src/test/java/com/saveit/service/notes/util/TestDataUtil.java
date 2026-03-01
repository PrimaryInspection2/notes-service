package com.saveit.service.notes.util;

import com.saveit.service.notes.repository.entity.NoteEntity;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.web.dto.*;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@UtilityClass
public class TestDataUtil {


    // ---------------- TAG DTO/ENTITY ----------------
    public static TagDto createTagDto(String tagId, String name) {
        return new TagDto(tagId, name, "Red", "Description " + name);
    }

    public static TagEntity createTagEntity(String tagId, String name, String userId) {
        TagEntity entity = new TagEntity();
        entity.setTagId(tagId);
        entity.setName(name);
        entity.setUserId(userId);
        entity.setColor("Red");
        entity.setDescription("Description " + name);
        return entity;
    }

    // ---------------- NOTE SERVICE DTO/RESPONSE ----------------
    public static NoteServiceRequestDto createNoteRequest(String noteId, String userId) {
        return NoteServiceRequestDto.builder()
                .noteId(noteId)
                .userId(userId)
                .title("Title " + noteId)
                .content("Content " + noteId)
                .status(NoteStatus.ACTIVE)
                .priority(NotePriority.MEDIUM)
                .tags(Collections.emptySet())
                .build();
    }

    public static NoteResponseDto createNoteResponse(String noteId, String userId) {
        return NoteResponseDto.builder()
                .noteId(noteId)
                .userId(userId)
                .title("Title " + noteId)
                .content("Content " + noteId)
                .status(NoteStatus.ACTIVE)
                .priority(NotePriority.MEDIUM)
                .source(NoteSource.REST_API)
                .tags(Collections.emptySet())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static GetNotesRequestDto createGetNotesRequest(String userId) {
        return new GetNotesRequestDto(userId, Collections.emptySet());
    }

    // ---------------- NOTE ENTITY ----------------
    public static NoteEntity createNoteEntity(String noteId, String userId) {
        NoteEntity entity = new NoteEntity();
        entity.setNoteId(noteId);
        entity.setUserId(userId);
        entity.setTitle("Title " + noteId);
        entity.setContent("Content " + noteId);
        entity.setStatus(NoteStatus.ACTIVE);
        entity.setPriority(NotePriority.MEDIUM);
        entity.setSource(NoteSource.REST_API);
        entity.setTags(Collections.emptySet());
        return entity;
    }

    public static NoteEntity createNoteEntityWithTags(String noteId, String userId, Set<TagEntity> tags) {
        NoteEntity entity = createNoteEntity(noteId, userId);
        entity.setTags(tags);
        return entity;
    }
}