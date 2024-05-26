package de.sparkarmy.jda.utils;

import java.util.Arrays;
import java.util.List;

public enum LogChannelType {
    UNKNOW(-1,"unknow"),
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

    public static List<LogChannelType> getLogChannelTypes(){
        return Arrays.stream(LogChannelType.values()).toList();
    }

    public static LogChannelType getLogChannelTypeByName(String name){
        List<LogChannelType> validValues = Arrays.stream(LogChannelType.values()).filter(x->x.getName().equals(name)).toList();
        if (validValues.isEmpty()) return UNKNOW;
        return validValues.getFirst();
    }
}
