package ru.practicum.explorewithme.client;

import jakarta.annotation.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BaseClient {
    protected final RestTemplate rest;
    private final String baseUrl;

    public BaseClient(RestTemplate restTemplate, String baseUrl) {
        this.rest = restTemplate;
        this.baseUrl = baseUrl;
    }

    private static HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());
        try {
            return rest.exchange(baseUrl + path, HttpMethod.POST, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    protected <T> ResponseEntity<T> get(
            String path,
            @Nullable MultiValueMap<String, String> parameters,
            ParameterizedTypeReference<T> responseType
    ) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(defaultHeaders());

        try {
            String url = parameters != null
                    ? UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                    .queryParams(parameters)
                    .build()
                    .toUriString()
                    : baseUrl + path;

            return rest.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );
        } catch (HttpStatusCodeException statusCodeException) {
            return ResponseEntity.status(statusCodeException.getStatusCode()).body(null);
        }
    }
}
