package com.saveit.service.notes.web.dto;


public record TagDto(String tagId, String name, String color, String description) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagDto other)) return false;
        return tagId.equals(other.tagId) && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return 31 * tagId.hashCode() + name.hashCode();
    }
}