package de.SparkArmy.utils.jda.mediaOnlyUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum MediaOnlyChannelActions {
    ADD(1,"add","Add a channel to media-only-channel list"),
    EDIT(2,"edit","Edit a channel in media-only-channel list"),
    REMOVE(3,"remove","Remove a channel from media-only-channel list"),

    UNKNOWN(-1,"unknown","Unknown action-type")
    ;

    private final int id;
    private final String name;
    private final String description;

    MediaOnlyChannelActions(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Contract(pure = true)
    public static MediaOnlyChannelActions getActionByName(@NotNull String name){
        switch (name){
            case "add" -> {
                return ADD;
            }
            case "edit" -> {
                return EDIT;
            }
            case "remove" -> {
                return REMOVE;
            }
            default -> {
                return UNKNOWN;
            }
        }
    }
}
