package com.saveit.service.notes.repository;

import com.saveit.service.notes.repository.entity.NoteEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface NoteRepository extends JpaRepository<@NonNull NoteEntity, @NonNull String> {

    Set<NoteEntity> findAllByUserId(String userId);

    Set<NoteEntity> findDistinctByUserIdAndTags_TagIdIn(String userId, Set<String> tagIds);

}
