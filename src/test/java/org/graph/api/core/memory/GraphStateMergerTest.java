package org.graph.api.core.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphStateMergerTest {

    @Test
    void shouldMergeBeanPropertiesAndBooleanIsGetterByDefault() {
        DefaultState source = new DefaultState();
        source.setName("Alice");
        source.setActive(true);

        DefaultState target = new DefaultState();
        target.setName("Bob");
        target.setActive(false);

        GraphStateMerger.merge(source, target);

        assertEquals("Alice", target.getName());
        assertTrue(target.isActive());
    }

    @Test
    void shouldSkipPropertyWhenGetterExcludedByMethod() {
        GetterExcludedState source = new GetterExcludedState();
        source.setValue("new");

        GetterExcludedState target = new GetterExcludedState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("old", target.getValue());
    }

    @Test
    void shouldSkipPropertyWhenSetterExcludedByMethod() {
        SetterExcludedState source = new SetterExcludedState();
        source.setValue("new");

        SetterExcludedState target = new SetterExcludedState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("old", target.getValue());
    }

    @Test
    void shouldApplyFieldExcludeForGetterAndSetter() {
        FieldExcludedState source = new FieldExcludedState();
        source.setValue("new");

        FieldExcludedState target = new FieldExcludedState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("old", target.getValue());
    }

    @Test
    void shouldAllowMethodExcludeOverrideFieldExclude() {
        MethodPriorityOverFieldState source = new MethodPriorityOverFieldState();
        source.setValue("new");

        MethodPriorityOverFieldState target = new MethodPriorityOverFieldState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("new", target.getValue());
    }

    @Test
    void shouldSupportDeprecatedSavePointIgnore() {
        IgnoreState source = new IgnoreState();
        source.setValue("new");

        IgnoreState target = new IgnoreState();
        target.setValue("old");

        GraphStateMerger.merge(source, target);

        assertEquals("old", target.getValue());
    }

    @Test
    void shouldResolveIncludeKeysAndDefaultsFromField() {
        ClassMetadata metadata = AnnotationResolver.resolveClassMetadata(IncludeFieldState.class);
        PropertyMetadata property = metadata.properties().get("name");

        assertTrue(property.writeGetterIncluded());
        assertFalse(property.writeSetterIncluded());
        assertEquals("player_name", property.writeKey());

        assertFalse(property.readGetterIncluded());
        assertTrue(property.readSetterIncluded());
        assertEquals("player_name", property.readKey());
    }

    @Test
    void shouldUsePropertyNameAsDefaultKeyWhenIncludeKeyIsEmpty() {
        ClassMetadata metadata = AnnotationResolver.resolveClassMetadata(DefaultKeyIncludeState.class);
        PropertyMetadata property = metadata.properties().get("name");

        assertEquals("name", property.writeKey());
        assertEquals("name", property.readKey());
    }

    @Test
    void shouldUseMethodKeyOverFieldKey() {
        ClassMetadata metadata = AnnotationResolver.resolveClassMetadata(MethodKeyPriorityState.class);
        PropertyMetadata property = metadata.properties().get("value");

        assertEquals("getter_key", property.writeKey());
        assertEquals("setter_key", property.readKey());
    }

    @Test
    void shouldFailOnDuplicateWriteKey() {
        assertThrows(IllegalStateException.class, () -> AnnotationResolver.resolveClassMetadata(DuplicateWriteKeyState.class));
    }

    @Test
    void shouldFailOnDuplicateReadKey() {
        assertThrows(IllegalStateException.class, () -> AnnotationResolver.resolveClassMetadata(DuplicateReadKeyState.class));
    }

    private static class DefaultState {
        private String name;
        private boolean active;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    private static class GetterExcludedState {
        private String value;

        @SavePointExclude(getter = true, setter = false)
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class SetterExcludedState {
        private String value;

        public String getValue() {
            return value;
        }

        @SavePointExclude(getter = false, setter = true)
        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class FieldExcludedState {
        @SavePointExclude
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class MethodPriorityOverFieldState {
        @SavePointExclude(getter = true, setter = true)
        private String value;

        @SavePointExclude(getter = false, setter = false)
        public String getValue() {
            return value;
        }

        @SavePointExclude(getter = false, setter = false)
        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class IgnoreState {
        private String value;

        @SavePointIgnore
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class IncludeFieldState {
        @SavePointWriteInclude(key = "player_name")
        @SavePointReadInclude(key = "player_name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class DefaultKeyIncludeState {
        @SavePointWriteInclude
        @SavePointReadInclude
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class MethodKeyPriorityState {
        @SavePointWriteInclude(key = "field_key")
        @SavePointReadInclude(key = "field_key")
        private String value;

        @SavePointWriteInclude(key = "getter_key")
        public String getValue() {
            return value;
        }

        @SavePointReadInclude(key = "setter_key")
        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class DuplicateWriteKeyState {
        @SavePointWriteInclude(key = "hp")
        private int currentHp;

        @SavePointWriteInclude(key = "hp")
        private int maxHp;

        public int getCurrentHp() {
            return currentHp;
        }

        public void setCurrentHp(int currentHp) {
            this.currentHp = currentHp;
        }

        public int getMaxHp() {
            return maxHp;
        }

        public void setMaxHp(int maxHp) {
            this.maxHp = maxHp;
        }
    }

    private static class DuplicateReadKeyState {
        private int currentHp;
        private int maxHp;

        public int getCurrentHp() {
            return currentHp;
        }

        @SavePointReadInclude(key = "hp")
        public void setCurrentHp(int currentHp) {
            this.currentHp = currentHp;
        }

        public int getMaxHp() {
            return maxHp;
        }

        @SavePointReadInclude(key = "hp")
        public void setMaxHp(int maxHp) {
            this.maxHp = maxHp;
        }
    }
}
