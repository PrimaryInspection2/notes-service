package com.saveit.service.notes.repository.entity;

import com.saveit.service.notes.web.dto.NotePriority;
import com.saveit.service.notes.web.dto.NoteSource;
import com.saveit.service.notes.web.dto.NoteStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "notes")
@Entity
public class NoteEntity implements Persistable<@NonNull String> {

    @EqualsAndHashCode.Include
    @Id
    private String noteId;

    //fixme implement it when user-service ready
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String userId;

    @Column
    private String title;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotePriority priority;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @BatchSize(size = 50)
    private Set<TagEntity> tags = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;

    @Override
    public @Nullable String getId() {
        return noteId;
    }

    @Override
    public boolean isNew() {
        return Objects.isNull(createdAt) && Objects.isNull(updatedAt);
    }
}
