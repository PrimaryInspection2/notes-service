package com.saveit.service.notes.util;

import org.springframework.jdbc.core.JdbcTemplate;

public class DataUtil {

    public static void cleanUpTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM note_tags");
        jdbcTemplate.execute("DELETE FROM notes");
        jdbcTemplate.execute("DELETE FROM tags");
    }
}
