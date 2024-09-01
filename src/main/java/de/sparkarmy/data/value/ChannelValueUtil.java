package de.sparkarmy.data.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChannelValueUtil extends AbstractValueUtil {

    public static @NotNull List<ChannelValue> getChannelValues(long raw) {
        List<ChannelValue> values = new ArrayList<>();
        for (ChannelValue value : ChannelValue.values()) {
            if (isApplied(value.getValueRaw(), raw)) {
                values.add(value);
            }
        }
        return values;
    }
}
