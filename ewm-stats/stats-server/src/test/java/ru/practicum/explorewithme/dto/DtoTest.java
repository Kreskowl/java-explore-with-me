package ru.practicum.explorewithme.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.statsdto.Constants;
import ru.practicum.statsdto.StatDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureJsonTesters
@JsonTest
public class DtoTest {
    @Autowired
    private JacksonTester<StatDto> json;

    @Test
    void shouldSerializeAndDeserializeCorrectly() throws Exception {
        StatDto dto = new StatDto("app", "/uri", "127.0.0.1", LocalDateTime.now());

        String content = json.write(dto).getJson();
        StatDto deserialized = json.parse(content).getObject();

        assertEquals(dto.getApp(), deserialized.getApp());
    }

    @Test
    void shouldUseConstant() {
        assertEquals("^/.*", Constants.URI_STARTS_WITH_SLASH);
    }
}
