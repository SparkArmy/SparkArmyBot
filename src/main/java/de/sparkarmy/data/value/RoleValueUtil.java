package de.sparkarmy.data.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RoleValueUtil extends AbstractValueUtil {

    public static @NotNull List<RoleValue> getRoleValues(long raw) {
        List<RoleValue> values = new ArrayList<>();
        for (RoleValue value : RoleValue.values()) {
            if (isApplied(value.getValueRaw(), raw)) {
                values.add(value);
            }
        }
        return values;
    }
}
