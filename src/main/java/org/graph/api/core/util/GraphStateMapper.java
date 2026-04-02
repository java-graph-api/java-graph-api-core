package org.graph.api.core.util;

import lombok.Getter;
import org.graph.api.core.memory.SavePointIgnore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GraphStateMapper {

    private static final Map<Class<?>, Methods> cache = new ConcurrentHashMap<>();

    public static Object merge(Object source, Object target) {
        Methods methods = getMethods(source, target);

        Map<String, Method> getters = methods.getGetters();
        Map<String, Method> setters = methods.getSetters();

        setters.forEach((k, v) -> {
            try {
                Object value = getters.get(k).invoke(source);
                v.invoke(target, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        return target;
    }

    private static Methods getMethods(Object source, Object target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target must not be null");
        }

        if (!source.getClass().equals(target.getClass())) {
            throw new IllegalArgumentException(
                    "Source and target must be of the same class. " +
                            "Found: " + source.getClass().getName() +
                            " and " + target.getClass().getName()
            );
        }

        Class<?> clazz = source.getClass();

        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        } else {
            Methods methods = initMethods(clazz);
            cache.put(clazz, methods);
            return methods;
        }
    }

    private static Methods initMethods(Class<?> clazz) {
        Methods methods = new Methods();

        for (Method method : clazz.getMethods()) {
            if (isNotIgnore(method)) {
                if (isGetter(method)) {
                    methods.addGetter(method);
                } else if (isSetter(method)) {
                    methods.addSetter(method);
                }
            }
        }

        return methods;
    }

    private static String setterToField(String methodName) {
        if (!methodName.startsWith("set") || methodName.length() == 3) {
            return methodName;
        }

        String field = methodName.substring(3);
        return Character.toLowerCase(field.charAt(0)) + field.substring(1);
    }

    private static boolean isSetter(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }

        if (!method.getName().startsWith("set")) {
            return false;
        }

        return method.getParameterCount() == 1;
    }

    private static boolean isNotIgnore(Method method) {
        return !method.isAnnotationPresent(SavePointIgnore.class);
    }

    private static boolean isGetter(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }

        if (method.getParameterCount() != 0) {
            return false;
        }

        if (method.getReturnType().equals(void.class)) {
            return false;
        }

        String name = method.getName();

        if (name.startsWith("is") && method.getReturnType().equals(boolean.class)) {
            return true;
        }

        return name.startsWith("get") && name.length() > 3;
    }

    private static String getterToField(String methodName) {
        String field;

        if (methodName.startsWith("get")) {
            field = methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            field = methodName.substring(2);
        } else {
            return methodName;
        }

        if (field.isEmpty()) {
            return field;
        }

        return Character.toLowerCase(field.charAt(0)) + field.substring(1);
    }

    @Getter
    private static class Methods {

        private final Map<String, Method> getters = new HashMap<>();
        private final Map<String, Method> setters = new HashMap<>();

        public void addGetter(Method getter) {
            getters.put(getterToField(getter.getName()), getter);
        }

        public void addSetter(Method setter) {
            setters.put(setterToField(setter.getName()), setter);
        }
    }

}
