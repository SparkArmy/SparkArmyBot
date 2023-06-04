package de.SparkArmy.twitter.utils;

import org.json.JSONObject;

public interface User {

    String getName();

    String getId();

    String getUsername();

    JSONObject getData();
}
