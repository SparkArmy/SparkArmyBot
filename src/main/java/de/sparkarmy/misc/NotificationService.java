package de.sparkarmy.misc;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public enum NotificationService {
    YOUTUBE("youtube"),
    TWITCH("twitch");

    private final String serviceName;

    NotificationService(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public static @Nullable NotificationService getNotificationServiceByName(String serviceName) {
        List<NotificationService> notificationServices = Arrays.stream(NotificationService.values()).filter(x -> x.serviceName.equals(serviceName)).toList();
        if (notificationServices.isEmpty()) return null;
        return notificationServices.getFirst();
    }
}
