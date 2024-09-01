package de.sparkarmy.data.value;

import org.jetbrains.annotations.NotNull;

public enum MemberValue implements IValue {
    UNKNOWN(-1, "unknown"),
    MODERATOR(1, "moderator");


    private final long offset;
    private final long raw;
    private final String name;

    MemberValue(long offset, String name) {
        this.offset = offset;
        this.raw = 1L << offset;
        this.name = name;
    }


    @Override
    public long getValueRaw() {
        return raw;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    public String getName() {
        return name;
    }


    public static long getRaw(MemberValue @NotNull ... values) {
        long raw = 0;
        for (MemberValue value : values) {
            if (value != null && value != UNKNOWN) {
                raw |= value.raw;
            }
        }
        return raw;
    }
}
