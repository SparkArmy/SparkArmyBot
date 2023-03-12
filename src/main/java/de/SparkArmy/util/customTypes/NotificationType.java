package de.SparkArmy.util.customTypes;

import org.jetbrains.annotations.NotNull;

public enum NotificationType {
    UNKNOW(-1,"unknow"),
    NEWS(1,"news"),
    YOUTUBE(2,"youtube"),
    TWITTER(3,"twitter"),
    TWITCH(4,"twitch"),
    TIKTOK(5,"ticktok")
    ;


    private final int id;
    private final String typeName;

    NotificationType(int id, String typeName) {
        this.id = id;
        this.typeName = typeName;
    }

    public int getId() {
        return id;
    }

    public String getTypeName() {
        return typeName;
    }

    public static NotificationType getNotificationTypeByName(@NotNull String typeName){
        switch (typeName){
            case "news" -> {
                return NEWS;
            }
            case "youtube" -> {
                return YOUTUBE;
            }
            case "twitter" -> {
                return TWITTER;
            }
            case "twitch" -> {
                return TWITCH;
            }
            case "tiktok" -> {
                return TIKTOK;
            }
            default -> {
                return UNKNOW;
            }
        }
    }
}
