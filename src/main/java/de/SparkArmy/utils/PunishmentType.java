package de.SparkArmy.utils;

@SuppressWarnings("unused")
public enum PunishmentType {
    WARN(1,"warn","Warns a user"),
    MUTE(2,"mute","Mute a user"),
    KICK(3,"kick","Kick a user"),
    BAN(4,"ban","Ban a user"),
    UNBAN(5,"unban","Unban a user"),
    TIMEOUT(6,"timeout","Timeout a user");


    private final Integer id;
    private final String name;
    private final String description;

    PunishmentType(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
