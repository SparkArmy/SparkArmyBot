package de.SparkArmy.twitter.utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class TwitterUser implements User {

    private final JSONObject data;
    private final String name;
    private final String id;
    private final String username;

    public TwitterUser(@NotNull JSONObject rawResponse) {
        this.data = rawResponse.getJSONObject("data");
        this.name = data.getString("name");
        this.id = data.getString("id");
        this.username = data.getString("username");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public JSONObject getData() {
        return data;
    }
}
