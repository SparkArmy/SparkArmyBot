package de.SparkArmy.utils.punishmentUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public enum PunishmentType {
    WARN(1,"warn","Warns a user"),
    MUTE(2,"mute","Mute a user"),
    KICK(3,"kick","Kick a user"),
    BAN(4,"ban","Ban a user"),
    UNBAN(5,"unban","Unban a user"),
    TIMEOUT(6,"timeout","Timeout a user"),
    UNKNOW(-1,"unknow","Unkow punishment");


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

    @Contract(pure = true)
    public static PunishmentType getByName(@NotNull String name){
        return Arrays.stream(PunishmentType.values()).filter(x->x.getName().equals(name)).findAny().orElse(UNKNOW);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull List<String> getAllTypes(){
        return new ArrayList<>(){{
            add(WARN.getName());
            add(MUTE.getName());
            add(KICK.getName());
            add(BAN.getName());
            add(UNBAN.getName());
            add(TIMEOUT.getName());
        }};
    }
}
