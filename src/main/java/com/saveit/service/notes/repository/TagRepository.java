package com.saveit.service.notes.repository;

import com.saveit.service.notes.repository.entity.TagEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<@NonNull TagEntity, @NonNull String> {

    Set<TagEntity> findAllByUserIdAndNameIn(String userId, Set<String> tags);
}