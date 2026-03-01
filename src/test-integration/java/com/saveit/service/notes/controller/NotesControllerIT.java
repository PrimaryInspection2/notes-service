package com.saveit.service.notes.controller;

import com.saveit.service.notes.service.NoteService;
import com.saveit.service.notes.web.controller.NotesController;
import com.saveit.service.notes.web.dto.GetNotesRequestDto;
import com.saveit.service.notes.web.dto.NoteResponseDto;
import com.saveit.service.notes.web.dto.NoteServiceRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static com.saveit.service.notes.util.TestDataUtil.createNoteRequest;
import static com.saveit.service.notes.util.TestDataUtil.createNoteResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("it")
@WebMvcTest(controllers = NotesController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotesControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    private NoteServiceRequestDto noteServiceRequestDto;
    private GetNotesRequestDto getNotesRequestDto;
    private NoteResponseDto noteResponseDto;

    @BeforeEach
    void setUp() {
        getNotesRequestDto = new GetNotesRequestDto("user1", null);

        noteServiceRequestDto = createNoteRequest("note1", "user1");

        noteResponseDto = createNoteResponse("note1", "user1");
    }

    @Test
    void processNote_shouldReturnCreatedNote_whenValidRequest() throws Exception {
        when(noteService.processNote(any(NoteServiceRequestDto.class)))
                .thenReturn(noteResponseDto);

        mockMvc.perform(post("/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteServiceRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteId").value("note1"))
                .andExpect(jsonPath("$.content").value("Content " + noteResponseDto.noteId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<NoteServiceRequestDto> captor = ArgumentCaptor.forClass(NoteServiceRequestDto.class);
        verify(noteService).processNote(captor.capture());

        assertEquals("user1", captor.getValue().userId());
    }

    @Test
    void processNote_shouldReturn500_whenServiceFails() throws Exception {
        when(noteService.processNote(any()))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteServiceRequestDto)))
                .andExpect(status().isInternalServerError());

        verify(noteService).processNote(any());
    }

    @Test
    void getById_shouldReturnNote_whenExists() throws Exception {
        when(noteService.getById("note1")).thenReturn(noteResponseDto);

        mockMvc.perform(get("/note/{id}", "note1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteId").value("note1"))
                .andExpect(jsonPath("$.userId").value("user1"));

        verify(noteService).getById("note1");
    }

    @Test
    void getById_shouldReturn500_whenNotFound() throws Exception {
        when(noteService.getById("note1")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/note/{id}", "note1"))
                .andExpect(status().isInternalServerError());

        verify(noteService).getById("note1");
    }

    @Test
    void delete_shouldReturn200_whenSuccess() throws Exception {
        doNothing().when(noteService).delete("note1");

        mockMvc.perform(delete("/note/{id}", "note1"))
                .andExpect(status().isOk());

        verify(noteService).delete("note1");
    }

    @Test
    void delete_shouldReturn500_whenServiceFails() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(noteService).delete("note1");

        mockMvc.perform(delete("/note/{id}", "note1"))
                .andExpect(status().isInternalServerError());

        verify(noteService).delete("note1");
    }

    @Test
    void getAll_shouldReturnList_whenNotesExist() throws Exception {
        when(noteService.getAllByUserId(getNotesRequestDto))
                .thenReturn(Set.of(noteResponseDto));

        mockMvc.perform(get("/note/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getNotesRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].noteId").value("note1"))
                .andExpect(jsonPath("$[0].content").value("Content note1"));

        verify(noteService).getAllByUserId(getNotesRequestDto);
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoNotes() throws Exception {
        when(noteService.getAllByUserId(getNotesRequestDto))
                .thenReturn(Set.of());

        mockMvc.perform(get("/note/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getNotesRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(noteService).getAllByUserId(getNotesRequestDto);
    }

    @Test
    void getAll_shouldReturn500_whenServiceFails() throws Exception {
        when(noteService.getAllByUserId(getNotesRequestDto))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/note/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getNotesRequestDto)))
                .andExpect(status().isInternalServerError());

        verify(noteService).getAllByUserId(getNotesRequestDto);
    }
}