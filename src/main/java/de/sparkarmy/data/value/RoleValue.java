package de.sparkarmy.data.value;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum RoleValue implements IValue {
    UNKNOWN(-1, false, false, "Unknown"),
    MUTE_ROLE(1, true, false, "Mute-Role"),
    WARN_ROLE(2, true, false, "Warn-Role"),
    TICKET_ROLE(3, false, true, "Ticket-Ping-Role");

    private final long offset;
    private final long raw;
    private final boolean isPunishment;
    private final boolean isTicket;
    private final String name;

    RoleValue(long offset, boolean isPunishment, boolean isTicket, String name) {
        this.offset = offset;
        this.raw = 1L << offset;
        this.isPunishment = isPunishment;
        this.isTicket = isTicket;
        this.name = name;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public long getValueRaw() {
        return raw;
    }

    public boolean isPunishment() {
        return isPunishment;
    }

    public boolean isTicket() {
        return isTicket;
    }

    public String getName() {
        return name;
    }

    @Contract(mutates = "this")
    public static long getRaw(RoleValue @NotNull ... values) {
        long raw = 0;
        for (RoleValue value : values) {
            if (value != null && value != UNKNOWN) {
                raw |= value.raw;
            }
        }
        return raw;
    }

    public static long getAllValuesRaw() {
        return getRaw(RoleValue.values());
    }

    public static RoleValue getByName(String name) {
        return Arrays.stream(RoleValue.values()).filter(x -> x.getName().equals(name)).findFirst().orElse(UNKNOWN);
    }
}
