package com.saveit.service.notes.controller;

import com.saveit.service.notes.config.TestContainersConfig;
import com.saveit.service.notes.config.TestRestTemplateConfig;
import com.saveit.service.notes.repository.NoteRepository;
import com.saveit.service.notes.repository.TagRepository;
import com.saveit.service.notes.repository.entity.NoteEntity;
import com.saveit.service.notes.repository.entity.TagEntity;
import com.saveit.service.notes.util.DataUtil;
import com.saveit.service.notes.web.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@ActiveProfiles("it")
@SpringBootTest(classes = {TestContainersConfig.class, TestRestTemplateConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotesControllerE2ETest {

    @Autowired private RestTemplate restTemplate;
    @Autowired private NoteRepository noteRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    // --- shared test data IDs (used across nested classes) ---
    private static final String USER_1       = "user-1";
    private static final String USER_2       = "user-2";
    private static final String NOTE_10      = "note-10";
    private static final String TAG_1        = "tag-1";
    private static final String TAG_2        = "tag-2";
    private static final String TAG_3        = "tag-3";

    private String baseUrl() {
        return "http://localhost:" + port + "/service-notes/note";
    }

    @AfterEach
    void cleanDatabase() {
        DataUtil.cleanUpTables(jdbcTemplate);
    }
    // ============================================================
    // CREATE / UPDATE
    // ============================================================

    @Nested
    class ProcessNote {

        @Test
        void shouldCreateNewNoteWhenNotExists() {
            NoteServiceRequestDto request = NoteServiceRequestDto.builder()
                    .userId(USER_1)
                    .title("New title")
                    .content("New content")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(Set.of())
                    .build();

            ResponseEntity<NoteResponseDto> response = restTemplate.postForEntity(baseUrl(), request, NoteResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            String generatedId = response.getBody().noteId();
            assertThat(generatedId).isNotBlank();
            assertThat(noteRepository.findById(generatedId)).isPresent();
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldUpdateExistingNote() {
            NoteServiceRequestDto request = NoteServiceRequestDto.builder()
                    .noteId(NOTE_10)
                    .userId(USER_1)
                    .title("Updated")
                    .content("Updated content")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.DONE)
                    .priority(NotePriority.CRITICAL)
                    .tags(Set.of())
                    .build();

            restTemplate.postForEntity(baseUrl(), request, NoteResponseDto.class);

            NoteEntity updated = noteRepository.findById(NOTE_10).orElseThrow();
            assertThat(updated.getTitle()).isEqualTo("Updated");
            assertThat(updated.getStatus()).isEqualTo(NoteStatus.DONE);
            assertThat(updated.getPriority()).isEqualTo(NotePriority.CRITICAL);
        }

        @Test
        void shouldCreateNoteWithNewTags() {
            NoteServiceRequestDto request = NoteServiceRequestDto.builder()
                    .userId(USER_1)
                    .title("title")
                    .content("content")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(Set.of(
                            new TagDto(null, "work", "#111111", "desc"),
                            new TagDto(null, "home", "#222222", "desc")
                    ))
                    .build();

            ResponseEntity<NoteResponseDto> response = restTemplate.postForEntity(baseUrl(), request, NoteResponseDto.class);

            String generatedId = response.getBody().noteId();
            NoteEntity saved = noteRepository.findByIdWithTags(generatedId).orElseThrow();

            assertThat(saved.getTags()).hasSize(2);
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void createNoteShouldReuseExistingTagsForSameUser() {
            NoteServiceRequestDto request = NoteServiceRequestDto.builder()
                    .userId(USER_1)
                    .title("reuse")
                    .content("reuse")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(Set.of(new TagDto(null, "work", "#UPD", "new desc")))
                    .build();

            ResponseEntity<NoteResponseDto> response = restTemplate.postForEntity(baseUrl(), request, NoteResponseDto.class);

            NoteEntity saved = noteRepository.findByIdWithTags(response.getBody().noteId()).orElseThrow();
            assertThat(saved.getTags()).hasSize(1);

            TagEntity tag = saved.getTags().iterator().next();
            assertThat(tag.getName()).isEqualTo("work");
            assertThat(tag.getColor()).isEqualTo("#UPD");
            assertThat(tag.getTagId()).isEqualTo(TAG_1);
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void createNoteShouldNotMixTagsBetweenUsers() {
            // user-1 already has "work" (tag-1) from init script
            // user-2 creates a note with tag "work" — must get a NEW tag, not tag-1
            NoteServiceRequestDto request = NoteServiceRequestDto.builder()
                    .userId(USER_2)
                    .title("isolation")
                    .content("isolation")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(Set.of(new TagDto(null, "work", "#111", "desc")))
                    .build();

            ResponseEntity<NoteResponseDto> response =
                    restTemplate.postForEntity(baseUrl(), request, NoteResponseDto.class);

            NoteEntity saved = noteRepository.findByIdWithTags(response.getBody().noteId()).orElseThrow();
            TagEntity tag = saved.getTags().iterator().next();

            // user-2's tag must belong to user-2
            assertThat(tag.getUserId()).isEqualTo(USER_2);
            // must be a brand new tag, not tag-1 which belongs to user-1
            assertThat(tag.getTagId()).isNotEqualTo(TAG_1);
            // tag-1 must still belong to user-1 and not be reassigned
            TagEntity originalTag = tagRepository.findById(TAG_1).orElseThrow();
            assertThat(originalTag.getUserId()).isEqualTo(USER_1);
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void updateTagShouldReplaceTagsOnUpdate() {
            NoteServiceRequestDto request = NoteServiceRequestDto.builder()
                    .noteId(NOTE_10)
                    .userId(USER_1)
                    .title("updated")
                    .content("updated")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(Set.of(new TagDto(null, "newTag", "#999999", "new")))
                    .build();

            restTemplate.postForEntity(baseUrl(), request, NoteResponseDto.class);

            NoteEntity updated = noteRepository.findByIdWithTags(NOTE_10).orElseThrow();

            assertThat(updated.getTags()).hasSize(1);
            assertThat(updated.getTags())
                    .extracting(TagEntity::getName)
                    .containsExactly("newTag");
        }

        @Test
        void createNoteShouldHandleDuplicateTagNamesInRequest() {
            Set<TagDto> tags = new HashSet<>();
            tags.add(new TagDto(null, "dup", "#111", "1"));
            tags.add(new TagDto(null, "dup", "#222", "2"));

            NoteServiceRequestDto request = NoteServiceRequestDto.builder()
                    .userId(USER_1)
                    .title("dup")
                    .content("dup")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(tags)
                    .build();

            ResponseEntity<NoteResponseDto> response = restTemplate.postForEntity(baseUrl(), request, NoteResponseDto.class);

            NoteEntity saved = noteRepository.findByIdWithTags(response.getBody().noteId()).orElseThrow();
            assertThat(saved.getTags()).hasSize(1);
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldNotViolateUniqueConstraintWhenSameTagSentTwiceAcrossNotes() {
            // if service didn't deduplicate — DB would throw unique constraint violation
            // this test proves service handles it gracefully
            NoteServiceRequestDto noteA = NoteServiceRequestDto.builder()
                    .userId(USER_1)
                    .title("a")
                    .content("a")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(Set.of(new TagDto(null, "work", "#AABBCC", "desc")))
                    .build();

            NoteServiceRequestDto noteB = NoteServiceRequestDto.builder()
                    .userId(USER_1)
                    .title("b")
                    .content("b")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(Set.of(new TagDto(null, "work", "#DDEEFF", "desc")))
                    .build();

            // neither of these should throw — service must reuse existing tag
            assertThatCode(() -> restTemplate.postForEntity(baseUrl(), noteA, NoteResponseDto.class))
                    .doesNotThrowAnyException();
            assertThatCode(() -> restTemplate.postForEntity(baseUrl(), noteB, NoteResponseDto.class))
                    .doesNotThrowAnyException();

            // and still only one "work" tag exists for user-1
            long workCount = tagRepository.findAll().stream()
                    .filter(t -> "work".equals(t.getName()) && USER_1.equals(t.getUserId()))
                    .count();
            assertThat(workCount).isEqualTo(1);
        }
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Nested
    class GetById {

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldReturnNoteById() {
            ResponseEntity<NoteResponseDto> response =
                    restTemplate.getForEntity(baseUrl() + "/" + NOTE_10, NoteResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().noteId()).isEqualTo(NOTE_10);
            assertThat(response.getBody().userId()).isEqualTo(USER_1);
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldReturnNoteWithTagsById() {
            ResponseEntity<NoteResponseDto> response =
                    restTemplate.getForEntity(baseUrl() + "/" + NOTE_10, NoteResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().tags()).hasSize(2);
            assertThat(response.getBody().tags())
                    .extracting(TagDto::name)
                    .containsExactlyInAnyOrder("work", "home");
        }
    }

    // ============================================================
    // DELETE
    // ============================================================

    @Nested
    class Delete {

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldDeleteNote() {
            restTemplate.delete(baseUrl() + "/" + NOTE_10);
            assertThat(noteRepository.findById(NOTE_10)).isEmpty();
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldLeaveOrphanTagsWhenRemovedFromNote() {
            // Initial state (from init script):
            //   note-10  →  tag-1 (work), tag-2 (home)
            //   note-11  →  tag-1 (work)
            //   note-12  →  tag-3 (sport)

            // Update note-10 with empty tag list — detaches all tags from the note
            NoteServiceRequestDto update = NoteServiceRequestDto.builder()
                    .noteId(NOTE_10)
                    .userId(USER_1)
                    .title("orphan test")
                    .content("orphan test")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(NotePriority.HIGH)
                    .tags(Set.of())
                    .build();

            restTemplate.postForEntity(baseUrl(), update, NoteResponseDto.class);

            // note-10 must have no tags after update
            NoteEntity updated = noteRepository.findByIdWithTags(NOTE_10).orElseThrow();
            assertThat(updated.getTags()).isEmpty();

            // State after update:
            //   note-11  →  tag-1  (tag-1 still referenced, NOT an orphan)
            //   note-12  →  tag-3  (tag-3 still referenced, NOT an orphan)
            //   tag-2             (no longer referenced by any note = orphan)
            //
            // CascadeType.REMOVE is intentionally absent on @ManyToMany —
            // detaching a tag from a note must NOT delete the tag from the tags table.
            // Orphan cleanup is delegated to a scheduled job (see NoteEntity todo).
            Integer orphans = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM tags t
            LEFT JOIN note_tags nt ON t.tag_id = nt.tag_id
            WHERE nt.tag_id IS NULL
            """, Integer.class);

            assertThat(orphans).isEqualTo(1);
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldDeleteNoteAndCascadeJoinTableButKeepTags() {
            restTemplate.delete(baseUrl() + "/" + NOTE_10);

            // note deleted
            assertThat(noteRepository.findById(NOTE_10)).isEmpty();

            // join table cleared
            Integer joinCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM note_tags WHERE note_id = ?",
                    Integer.class, NOTE_10
            );
            assertThat(joinCount).isZero();

            // but tags exist in tag table
            assertThat(tagRepository.findAll())
                    .extracting(TagEntity::getTagId)
                    .contains(TAG_1, TAG_2);
        }
    }

    // ============================================================
    // SEARCH / FILTER
    // ============================================================

    @Nested
    class NotesSearch {

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldReturnAllNotesForUser() {
            GetNotesRequestDto request = GetNotesRequestDto.builder()
                    .userId(USER_1)
                    .tagIds(Set.of())
                    .build();

            ResponseEntity<Set> response = restTemplate.exchange(
                    baseUrl() + "/search",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    Set.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldFilterByTags() {
            // tag-1 ("work") linked to note-10 and note-11 (both for user-1)
            GetNotesRequestDto request = GetNotesRequestDto.builder()
                    .userId(USER_1)
                    .tagIds(Set.of(TAG_1))
                    .build();

            ResponseEntity<Set> response = restTemplate.exchange(
                    baseUrl() + "/search",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    Set.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldNotReturnNotesOfOtherUsers() {
            // user-2 searches by tag-1 — but tag-1 belongs to user-1, not user-2
            GetNotesRequestDto request = GetNotesRequestDto.builder()
                    .userId(USER_2)
                    .tagIds(Set.of(TAG_1))
                    .build();

            ResponseEntity<Set> response = restTemplate.exchange(
                    baseUrl() + "/search",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    Set.class
            );

            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldReturnEmptyWhenNoNotesForUser() {
            GetNotesRequestDto request = GetNotesRequestDto.builder()
                    .userId("unknown-user-id")
                    .tagIds(Set.of())
                    .build();

            ResponseEntity<Set> response = restTemplate.exchange(
                    baseUrl() + "/search",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    Set.class
            );

            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @Sql(scripts = "/sql/notes_with_tags_init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void shouldFilterByMultipleTagsWithoutDuplicates() {
            // note-10 has both tag-1 and tag-2 — must appear only once in results
            // note-11 has tag-1 — also returned
            // DISTINCT in query prevents note-10 from appearing twice
            GetNotesRequestDto request = GetNotesRequestDto.builder()
                    .userId(USER_1)
                    .tagIds(Set.of(TAG_1, TAG_2))
                    .build();

            ResponseEntity<Set> response = restTemplate.exchange(
                    baseUrl() + "/search",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    Set.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2); // note-10 and note-11, no duplicates
        }
    }

    // ============================================================
    // PRIORITY PARAMETERIZED
    // ============================================================

    @Nested
    class PriorityParameterized {

        @ParameterizedTest
        @EnumSource(NotePriority.class)
        void shouldCreateNoteWithAllPriorities(NotePriority priority) {

            NoteServiceRequestDto request = NoteServiceRequestDto.builder()
                    .noteId(null)
                    .userId(USER_1)
                    .title("t")
                    .content("c")
                    .source(NoteSource.REST_API)
                    .status(NoteStatus.ACTIVE)
                    .priority(priority)
                    .tags(Set.of())
                    .build();

            ResponseEntity<NoteResponseDto> response =
                    restTemplate.postForEntity(baseUrl(), request, NoteResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            NoteEntity saved = noteRepository.findById(response.getBody().noteId()).orElseThrow();
            assertThat(saved.getPriority()).isEqualTo(priority);
        }
    }
}