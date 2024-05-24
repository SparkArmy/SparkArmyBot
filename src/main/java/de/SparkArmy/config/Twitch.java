package de.SparkArmy.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Twitch(
        @JsonProperty("twitch-client-id") String clientId,
        @JsonProperty("twitch-client-secret") String secret
) {
}
