package de.sparkarmy.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Discord(
        @JsonProperty("discord-client-id") String clientId,
        @JsonProperty("discord-token") String token,
        @JsonProperty("log") String logWebhookUrl
) {
}
