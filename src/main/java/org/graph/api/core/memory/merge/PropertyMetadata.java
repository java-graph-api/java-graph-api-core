package org.graph.api.core.memory.merge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public record PropertyMetadata(
        String propertyName,
        Method getter,
        Method setter,
        Field field,
        boolean getterExcluded,
        boolean setterExcluded,
        boolean writeGetterIncluded,
        boolean writeSetterIncluded,
        boolean readGetterIncluded,
        boolean readSetterIncluded,
        String writeKey,
        String readKey,
        boolean writeIncludePresent,
        boolean readIncludePresent
) {

    public boolean isMergeEligible() {
        return getter != null && setter != null && !getterExcluded && !setterExcluded;
    }
}
