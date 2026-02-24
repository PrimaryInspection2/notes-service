package com.saveit.service.notes.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Objects;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@Entity
public class TagEntity implements Persistable<@NonNull String> {

    @EqualsAndHashCode.Include
    @Id
    private String tagId;

    //todo implement it when user-service ready
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String userId;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String name;

    @Column
    private String color;

    @Column
    private String description;

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
        return tagId;
    }

    @Override
    public boolean isNew() {
        return Objects.isNull(createdAt) && Objects.isNull(updatedAt);
    }
}