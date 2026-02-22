package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StatsClient {

    private final String serverUrl;
    private final RestTemplate restTemplate;

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplate restTemplate) {
        this.serverUrl = normalizeBaseUrl(Objects.requireNonNull(serverUrl, "serverUrl"));
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate");
    }

    public void addStats(EndpointHitDto endpointHitDto) {
        URI uri = URI.create(serverUrl + "/hit");
        RequestEntity<EndpointHitDto> request = RequestEntity
                .post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitDto);

        try {
            restTemplate.exchange(request, Void.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Stats service /hit failed: " + e.getStatusCode(), e);
        }
    }

    public List<ViewStatsDto> getStats(String start, String end, @Nullable List<String> uris, @Nullable Boolean unique) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(serverUrl)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParamIfPresent("uris", (uris == null || uris.isEmpty())
                        ? java.util.Optional.empty()
                        : java.util.Optional.of(uris))
                .queryParamIfPresent("unique", unique == null
                        ? java.util.Optional.empty()
                        : java.util.Optional.of(unique))
                .build(true)
                .encode()
                .toUri();

        try {
            var response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStatsDto>>() {}
            );
            return response.getBody() == null ? Collections.emptyList() : response.getBody();
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Stats service /stats failed: " + e.getStatusCode(), e);
        }
    }

    private static String normalizeBaseUrl(String baseUrl) {
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
