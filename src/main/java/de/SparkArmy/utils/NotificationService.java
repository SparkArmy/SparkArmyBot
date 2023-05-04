package de.SparkArmy.utils;

public enum NotificationService {
    YOUTUBE("youtube"),
    TWITCH("twitch"),
    TWITTER("twitter");

    private final String serviceName;

    NotificationService(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
