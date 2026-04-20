package org.graph.api.core.memory.merge;

import org.graph.api.core.memory.ClassMetadata;
import org.graph.api.core.memory.annotation.SavePointExclude;
import org.graph.api.core.memory.annotation.SavePointIgnore;
import org.graph.api.core.memory.annotation.SavePointInclude;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

final class AnnotationResolver {

    private AnnotationResolver() {
    }

    static ClassMetadata resolveClassMetadata(Class<?> clazz) {
        Map<String, MutablePropertyMeta> mutable = new LinkedHashMap<>();

        for (Method method : clazz.getMethods()) {
            if (isGetter(method)) {
                mutable.computeIfAbsent(getterToProperty(method.getName()), MutablePropertyMeta::new).getter = method;
            } else if (isSetter(method)) {
                mutable.computeIfAbsent(setterToProperty(method.getName()), MutablePropertyMeta::new).setter = method;
            }
        }

        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                mutable.computeIfAbsent(field.getName(), MutablePropertyMeta::new).field = field;
            }
            current = current.getSuperclass();
        }

        Map<String, PropertyMetadata> properties = new LinkedHashMap<>();
        for (MutablePropertyMeta meta : mutable.values()) {
            properties.put(meta.propertyName, toPropertyMetadata(meta));
        }

        validateUniqueKeys(clazz, properties, true);
        validateUniqueKeys(clazz, properties, false);

        return new ClassMetadata(properties);
    }

    private static void validateUniqueKeys(Class<?> clazz, Map<String, PropertyMetadata> properties, boolean writeOperation) {
        Map<String, String> keys = new HashMap<>();

        for (PropertyMetadata metadata : properties.values()) {
            boolean includePresent = writeOperation ? metadata.writeIncludePresent() : metadata.readIncludePresent();
            if (!includePresent) {
                continue;
            }

            String key = writeOperation ? metadata.writeKey() : metadata.readKey();
            String existingProperty = keys.putIfAbsent(key, metadata.propertyName());

            if (existingProperty != null && !existingProperty.equals(metadata.propertyName())) {
                throw new IllegalStateException(
                        "Duplicate " + (writeOperation ? "write" : "read") + " key '" + key + "' in class "
                                + clazz.getName() + " for properties: "
                                + existingProperty + ", " + metadata.propertyName()
                );
            }
        }
    }

    private static PropertyMetadata toPropertyMetadata(MutablePropertyMeta meta) {
        SavePointExclude getterExclude = annotationFromGetter(meta.getter, SavePointExclude.class);
        SavePointExclude setterExclude = annotationFromSetter(meta.setter, SavePointExclude.class);
        SavePointExclude fieldExclude = annotationFromField(meta.field, SavePointExclude.class);

        SavePointIgnore getterIgnore = annotationFromGetter(meta.getter, SavePointIgnore.class);
        SavePointIgnore setterIgnore = annotationFromSetter(meta.setter, SavePointIgnore.class);
        SavePointIgnore fieldIgnore = annotationFromField(meta.field, SavePointIgnore.class);

        boolean getterExcluded = resolveGetterExcluded(getterExclude, getterIgnore, fieldExclude, fieldIgnore);
        boolean setterExcluded = resolveSetterExcluded(setterExclude, setterIgnore, fieldExclude, fieldIgnore);

        SavePointInclude getterInclude = annotationFromGetter(meta.getter, SavePointInclude.class);
        SavePointInclude setterInclude = annotationFromSetter(meta.setter, SavePointInclude.class);
        SavePointInclude fieldInclude = annotationFromField(meta.field, SavePointInclude.class);

        boolean writeGetterIncluded = resolveWriteGetterIncluded(getterInclude, fieldInclude);
        boolean writeSetterIncluded = resolveWriteSetterIncluded(setterInclude, fieldInclude);
        boolean readGetterIncluded = resolveReadGetterIncluded(getterInclude, fieldInclude);
        boolean readSetterIncluded = resolveReadSetterIncluded(setterInclude, fieldInclude);

        String writeKey = resolveWriteKey(meta.propertyName, getterInclude, fieldInclude);
        String readKey = resolveReadKey(meta.propertyName, setterInclude, fieldInclude);

        boolean includePresent = getterInclude != null || setterInclude != null || fieldInclude != null;
        boolean writeIncludePresent = includePresent;
        boolean readIncludePresent = includePresent;

        return new PropertyMetadata(
                meta.propertyName,
                meta.getter,
                meta.setter,
                meta.field,
                getterExcluded,
                setterExcluded,
                writeGetterIncluded,
                writeSetterIncluded,
                readGetterIncluded,
                readSetterIncluded,
                writeKey,
                readKey,
                writeIncludePresent,
                readIncludePresent
        );
    }

    private static boolean resolveGetterExcluded(
            SavePointExclude methodExclude,
            SavePointIgnore methodIgnore,
            SavePointExclude fieldExclude,
            SavePointIgnore fieldIgnore
    ) {
        if (methodExclude != null) {
            return methodExclude.getter();
        }
        if (methodIgnore != null) {
            return true;
        }
        if (fieldExclude != null) {
            return fieldExclude.getter();
        }
        return fieldIgnore != null;
    }

    private static boolean resolveSetterExcluded(
            SavePointExclude methodExclude,
            SavePointIgnore methodIgnore,
            SavePointExclude fieldExclude,
            SavePointIgnore fieldIgnore
    ) {
        if (methodExclude != null) {
            return methodExclude.setter();
        }
        if (methodIgnore != null) {
            return true;
        }
        if (fieldExclude != null) {
            return fieldExclude.setter();
        }
        return fieldIgnore != null;
    }

    private static boolean resolveWriteGetterIncluded(SavePointInclude getterInclude, SavePointInclude fieldInclude) {
        if (getterInclude != null) {
            return getterInclude.getter();
        }
        return fieldInclude != null && fieldInclude.getter();
    }

    private static boolean resolveWriteSetterIncluded(SavePointInclude setterInclude, SavePointInclude fieldInclude) {
        if (setterInclude != null) {
            return setterInclude.setter();
        }
        return fieldInclude != null && fieldInclude.setter();
    }

    private static boolean resolveReadGetterIncluded(SavePointInclude getterInclude, SavePointInclude fieldInclude) {
        if (getterInclude != null) {
            return getterInclude.getter();
        }
        return fieldInclude != null && fieldInclude.getter();
    }

    private static boolean resolveReadSetterIncluded(SavePointInclude setterInclude, SavePointInclude fieldInclude) {
        if (setterInclude != null) {
            return setterInclude.setter();
        }
        return fieldInclude != null && fieldInclude.setter();
    }

    private static String resolveWriteKey(
            String defaultProperty,
            SavePointInclude getterInclude,
            SavePointInclude fieldInclude
    ) {
        if (getterInclude != null && !getterInclude.key().isEmpty()) {
            return getterInclude.key();
        }
        if (fieldInclude != null && !fieldInclude.key().isEmpty()) {
            return fieldInclude.key();
        }
        return defaultProperty;
    }

    private static String resolveReadKey(
            String defaultProperty,
            SavePointInclude setterInclude,
            SavePointInclude fieldInclude
    ) {
        if (setterInclude != null && !setterInclude.key().isEmpty()) {
            return setterInclude.key();
        }
        if (fieldInclude != null && !fieldInclude.key().isEmpty()) {
            return fieldInclude.key();
        }
        return defaultProperty;
    }

    private static <T extends Annotation> T annotationFromGetter(Method getter, Class<T> annotationClass) {
        return getter == null ? null : getter.getAnnotation(annotationClass);
    }

    private static <T extends Annotation> T annotationFromSetter(Method setter, Class<T> annotationClass) {
        return setter == null ? null : setter.getAnnotation(annotationClass);
    }

    private static <T extends Annotation> T annotationFromField(Field field, Class<T> annotationClass) {
        return field == null ? null : field.getAnnotation(annotationClass);
    }

    private static boolean isGetter(Method method) {
        if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0) {
            return false;
        }

        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return !void.class.equals(method.getReturnType());
        }

        return name.startsWith("is") && name.length() > 2 && boolean.class.equals(method.getReturnType());
    }

    private static boolean isSetter(Method method) {
        return !Modifier.isStatic(method.getModifiers())
                && method.getName().startsWith("set")
                && method.getName().length() > 3
                && method.getParameterCount() == 1;
    }

    private static String getterToProperty(String methodName) {
        if (methodName.startsWith("get")) {
            return Introspector.decapitalize(methodName.substring(3));
        }
        return Introspector.decapitalize(methodName.substring(2));
    }

    private static String setterToProperty(String methodName) {
        return Introspector.decapitalize(methodName.substring(3));
    }

    private static final class MutablePropertyMeta {
        private final String propertyName;
        private Method getter;
        private Method setter;
        private Field field;

        private MutablePropertyMeta(String propertyName) {
            this.propertyName = propertyName;
        }
    }
}
