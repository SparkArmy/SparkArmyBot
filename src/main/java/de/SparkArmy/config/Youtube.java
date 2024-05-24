package de.SparkArmy.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Youtube(
        @JsonProperty("youtube-api-key") String apiKey,
        @JsonProperty("spring-callback-url") String callbackUrl
) {
}
