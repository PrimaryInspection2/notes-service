package com.saveit.service.notes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

@ActiveProfiles("it")
@SpringBootTest(classes = {NotesServiceApplication.class, TestcontainersConfiguration.class})
class NotesServiceApplicationIT {

    @Test
    void contextLoads() {
    }

}
