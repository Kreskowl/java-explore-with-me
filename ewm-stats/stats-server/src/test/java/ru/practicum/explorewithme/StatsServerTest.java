package ru.practicum.explorewithme;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StatsServerTest {
    @Test
    void contextLoads() {
        StatsApp.main(new String[]{});
    }
}

