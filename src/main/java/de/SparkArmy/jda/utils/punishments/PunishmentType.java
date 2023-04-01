package de.SparkArmy.jda.utils.punishments;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum PunishmentType {
    UNKNOWN(-1, "unknown"),
    WARN(1, "warn"),
    MUTE(2, "mute"),
    KICK(3, "kick"),
    BAN(4, "ban"),
    TIMEOUT(5, "timeout"),
    UNBAN(6, "unban"),
    SOFTBAN(7, "softban"),

    ;
    private final Integer id;
    private final String name;

    PunishmentType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Contract(" -> new")
    public @NotNull List<PunishmentType> getPunishmentTypes() {
        return new ArrayList<>(List.of(PunishmentType.values()));
    }
}
