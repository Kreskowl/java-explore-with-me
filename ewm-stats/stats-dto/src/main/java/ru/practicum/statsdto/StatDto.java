package ru.practicum.statsdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static ru.practicum.statsdto.Constants.IPV4_OR_IPV6;
import static ru.practicum.statsdto.Constants.URI_STARTS_WITH_SLASH;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class StatDto {
    @NotBlank
    private String app;
    @NotBlank
    @Pattern(regexp = URI_STARTS_WITH_SLASH, message = "URI must start with '/'")
    private String uri;

    @NotBlank
    @Pattern(regexp = IPV4_OR_IPV6, message = "Invalid IP address format")
    private String ip;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
