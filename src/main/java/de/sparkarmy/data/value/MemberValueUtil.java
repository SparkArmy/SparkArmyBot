package de.sparkarmy.data.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MemberValueUtil extends AbstractValueUtil {

    public static @NotNull List<MemberValue> getRoleValues(long raw) {
        List<MemberValue> values = new ArrayList<>();
        for (MemberValue value : MemberValue.values()) {
            if (isApplied(value.getValueRaw(), raw)) {
                values.add(value);
            }
        }
        return values;
    }
}
