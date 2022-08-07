package de.SparkArmy.utils;

@SuppressWarnings("unused")
public enum LogChannelType {
    MESSAGE(1,"message-log"),
    MEMBER (2,"member-log"),
    COMMAND (3,"command-log"),
    LEAVE (4,"leave-log"),
    MOD (5,"mod-log"),
    SERVER (6,"server-log"),
    VOICE (7,"voice-log");

    private final Integer id;
    private final String name;


    LogChannelType(Integer logId, String name) {
        id = logId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
