package com.travelsync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsync.model.Destination.WeatherInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;

@Component
public class OpenMeteoWeatherClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${travelsync.api.weather.url:https://api.open-meteo.com/v1/forecast}")
    private String weatherUrl;

    public OpenMeteoWeatherClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public WeatherInfo getWeather(double latitude, double longitude) {
        String uri = UriComponentsBuilder.fromHttpUrl(weatherUrl)
                .queryParam("latitude", String.format(Locale.US, "%.4f", latitude))
                .queryParam("longitude", String.format(Locale.US, "%.4f", longitude))
                .queryParam("current_weather", true)
                .toUriString();

        try {
            String response = restTemplate.getForObject(uri, String.class);
            if (response != null && !response.isBlank()) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode current = root.path("current_weather");
                if (!current.isMissingNode()) {
                    double temp = current.path("temperature").asDouble(20.0);
                    double wind = current.path("windspeed").asDouble(5.0);
                    int code = current.path("weathercode").asInt(0);
                    return new WeatherInfo(temp, wind, code, decodeWeatherCode(code));
                }
            }
        } catch (Exception ex) {
            log.warn("Could not retrieve live weather for ({}, {}): {}", latitude, longitude, ex.getMessage());
        }

        return new WeatherInfo(22.0, 8.0, 0, "Pleasant");
    }

    private String decodeWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Clear Sky";
            case 1, 2, 3 -> "Partly Cloudy";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Light Drizzle";
            case 61, 63, 65 -> "Rain Showers";
            case 71, 73, 75 -> "Snowfall";
            case 80, 81, 82 -> "Heavy Rain";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Fair";
        };
    }
}
