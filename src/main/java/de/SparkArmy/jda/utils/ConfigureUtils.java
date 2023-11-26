package de.SparkArmy.jda.utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ConfigureUtils {

    public ConfigureUtils() {

    }

    private final Map<String, JSONObject> regexStringMap = new HashMap<>();

    public void addRegexToMap(String key, JSONObject value) {
        regexStringMap.put(key, value);
    }

    public JSONObject getRegexByKey(String key) {
        return regexStringMap.getOrDefault(key, null);
    }

    public void removeRegexByKey(String key) {
        regexStringMap.remove(key);
    }

    public boolean isRegexNotInMap(String key) {
        return !regexStringMap.containsKey(key);
    }
}
