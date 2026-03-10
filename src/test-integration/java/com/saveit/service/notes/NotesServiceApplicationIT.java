package com.saveit.service.notes;

import com.saveit.service.notes.config.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("it")
@SpringBootTest(classes = {TestContainersConfig.class})
class NotesServiceApplicationIT {

    @Test
    void contextLoads() {
    }

}
