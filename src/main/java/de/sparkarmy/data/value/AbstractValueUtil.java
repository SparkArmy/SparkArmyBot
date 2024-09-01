package de.sparkarmy.data.value;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractValueUtil {

    public static boolean checkIsApplied(long raw, @NotNull ChannelValue value) {
        return isApplied(value.getValueRaw(), raw);
    }

    public static boolean checkIsApplied(long raw, IValue @NotNull ... values) {
        for (IValue value : values) {
            if (isApplied(value.getValueRaw(), raw)) return true;
        }
        return false;
    }

    protected static boolean isApplied(long values, long val) {
        return (values & val) != 0;
    }
}
