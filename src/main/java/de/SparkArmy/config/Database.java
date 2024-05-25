package de.SparkArmy.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Database(
        @JsonProperty("url") String url,
        @JsonProperty("user") String user,
        @JsonProperty("password") String password
) {
}
