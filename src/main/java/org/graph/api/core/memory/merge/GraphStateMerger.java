package org.graph.api.core.memory.merge;

import org.graph.api.core.memory.StateMetadata;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GraphStateMerger {

    private static final Map<Class<?>, StateMetadata> cache = new ConcurrentHashMap<>();

    public static Object merge(Object source, Object target) {
        StateMetadata stateMetadata = getClassMetadata(source, target);

        stateMetadata.properties().values().stream()
                .filter(PropertyMetadata::isMergeEligible)
                .forEach(propertyMetadata -> mergeProperty(source, target, propertyMetadata));

        return target;
    }

    private static void mergeProperty(Object source, Object target, PropertyMetadata propertyMetadata) {
        try {
            Object value = propertyMetadata.getter().invoke(source);
            propertyMetadata.setter().invoke(target, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static StateMetadata getClassMetadata(Object source, Object target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target must not be null");
        }

        if (!source.getClass().equals(target.getClass())) {
            throw new IllegalArgumentException(
                    "Source and target must be of the same class. "
                            + "Found: " + source.getClass().getName()
                            + " and " + target.getClass().getName()
            );
        }

        return cache.computeIfAbsent(source.getClass(), AnnotationResolver::resolveClassMetadata);
    }
}
